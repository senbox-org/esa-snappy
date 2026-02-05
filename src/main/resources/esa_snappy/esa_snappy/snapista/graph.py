import os
import subprocess
import tempfile
import platform
import lxml.etree as etree
import re
from esa_snappy import GPF
from xml.sax.saxutils import unescape
from esa_snappy.snapista.binning.output_bands import BinningOutputBands
from .target_band_descriptors import TargetBandDescriptors
from .binning import Aggregators
from .binning import BinningVariables


class Graph:
    """SNAP Graph class

    This class provides the methods to create, view and run a SNAP Graph

    Attributes:
        None.
    """
    def __init__(self, wdir=".", root=None):

        if root is None:
            self.root = etree.Element("graph")

            version = etree.SubElement(self.root, "version")
            version.text = "1.0"
        else:
            self.root = root

        self.pid = None
        self.p = None
        self.wdir = wdir
        self.gpt_path = self.get_gpt_cmd()

        if not self.gpt_path:
            raise Exception("gpt not found")

    def __str__(self):

        return "gpt binary: {}\nworking dir: {}\n\n{}".format(
            self.gpt_path,
            self.wdir,
            etree.tostring(self.root, pretty_print=True).decode("utf-8"),
        ).replace("\\n", "\n")

    def __repr__(self):

        return "Graph(wdir='{}')".format(self.wdir)

    @staticmethod
    def get_gpt_cmd():

        gpt_cmd = None

        if platform.system() == 'Windows':
            path_split_char = ";"
            gpt_executable = "gpt.exe"
        else:
            path_split_char = ":"
            gpt_executable = "gpt"

        for p in os.getenv("PATH").split(path_split_char):
            if os.path.exists(os.path.join(p, gpt_executable)):
                gpt_cmd = os.path.join(p, gpt_executable)
                break

        return gpt_cmd

    @staticmethod
    def list_operators():
        """This function provides a Python dictionary with all SNAP operators.

        Args:
            None.

        Returns
            Python dictionary with all SNAP operators.

        Raises:
            None.
        """
        GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis()

        op_spi_it = (
            GPF.getDefaultInstance()
            .getOperatorSpiRegistry()
            .getOperatorSpis()
            .iterator()
        )

        snap_operators = []

        while op_spi_it.hasNext():
            op_spi = op_spi_it.next()
            snap_operators.append(op_spi.getOperatorAlias())

        print('Number of operators provided by SNAP: ' + str(len(snap_operators))) 
        
        return snap_operators

    @staticmethod
    def describe_operators():
        """This function provides a Python dictionary with all SNAP operators.

        Args:
            None.

        Returns
            Python dictionary with all SNAP operators.

        Raises:
            None.
        """

        desc_dict = {}

        GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis()

        op_spi_it = (
            GPF.getDefaultInstance()
            .getOperatorSpiRegistry()
            .getOperatorSpis()
            .iterator()
        )

        while op_spi_it.hasNext():
            op_spi = op_spi_it.next()
            alias = op_spi.getOperatorDescriptor().getAlias()
            description = op_spi.getOperatorDescriptor().getDescription()
            src_prod_descriptors = op_spi.getOperatorDescriptor().getSourceProductDescriptors()
            if src_prod_descriptors is not None and len(src_prod_descriptors) > 0:
               src_prod_id = src_prod_descriptors.get(0) 
               print('describe_operators src_prod_id: ' + src_prod_id)

            desc_dict[alias] = description
            print("{} - {}".format(alias, description))

        return desc_dict


    def view(self):
        """This method prints SNAP Graph

        Args:
            None.

        Returns
            None.

        Raises:
            None.
        """
        #print(unescape(etree.tostring(self.root, pretty_print=True).decode("utf-8")).replace("&amp;", "&"))
        print(unescape(etree.tostring(self.root, pretty_print=True).decode("utf-8")))

    
    @staticmethod
    def remove_duplicate_tag(parameter_elem, root_tag):
        """This method removes duplicated tags in a xml etree element with a given root tag

        Args:
            parameter_elem: the XML element
            root_tag: the root tag of the element
        Returns
            None.

        Raises:
            None.
        """
        for tagn in parameter_elem.iter(root_tag):
            if tagn.getparent().getparent() is not None and tagn.getparent().getparent().tag == root_tag:
                ipar_of_parent = tagn.getparent().getparent()
                i_parent = tagn.getparent()
                if ipar_of_parent.text is None:
                    ipar_of_parent.addnext(i_parent)
                    ipar_of_parent.getparent().remove(ipar_of_parent)
            elif tagn.getparent() is not None and tagn.getparent().tag == root_tag:
                i_parent = tagn.getparent()
                if i_parent.text is None:
                    i_parent.addnext(tagn)
                    i_parent.getparent().remove(i_parent)
    
    
    def append_merge_op_nested_param(self, operator, parameter_elem, param):
        print('special case: nested parameter <' + param + '>' )
        # we want to get this:
        #     '<exclude><productId>Read</productId><namePattern>Oa.*radiance</namePattern></exclude><exclude><productId>vicarious</productId><namePattern>Oa12_radiance</namePattern></exclude>'
        # transformed to:
        #  <excludes>
        #    <exclude>
        #      <productId>Read</productId>
        #      <namePattern>Oa.*radiance</namePattern>
        #    </exclude>
        #    <exclude>
        #      <productId>vicarious</productId>
        #      <namePattern>Oa12_radiance</namePattern>
        #    </exclude>
        #  </excludes>                   
        
        open_tag = '<' + param + '>' 
        close_tag = '</' + param + '>'  
        nested_param = open_tag + getattr(operator, param) + close_tag
        if nested_param is not None:
            nested_param_elems = nested_param.split('</' + param + '>')

            pattern = '<' + param + '>' + '<' + param[:-1] + '>' + \
                      '<productId>.*</productId><namePattern>.*</namePattern>' + '</' + param[:-1] + '>'
            valid = True
            for elem in nested_param_elems:
                x = re.match(pattern, elem)
                print (elem)
                if x is None and len(elem) > 0:
                    valid = False
                    print('WARNING: malforatted nested parameter: ' + elem)
            
            if valid:
                parameter_elem.append(etree.fromstring(nested_param))
                self.remove_duplicate_tag(parameter_elem, param)
    
    
    def add_node(self, operator, node_id, source=None):
        """This method adds or overwrites a node to the SNAP Graph

        Args:
            operator: SNAP operator
            node_id: node identifier
            source: string or list of sources (previous node identifiers in the SNAP Graph)

        Returns
            None.

        Raises:
            None.
        """
        #print('entering add_node...')
        
        xpath_expr = '/graph/node[@id="%s"]' % node_id

        if len(self.root.xpath(xpath_expr)) != 0:

            node_elem = self.root.xpath(xpath_expr)[0]
            operator_elem = self.root.xpath(xpath_expr + "/operator")[0]
            parameters_elem = self.root.xpath(xpath_expr + "/parameters")

            for param in [
                name
                for name in dir(operator)
                if name[:2] != "__"
                and name[-2:] != "__"
                and name != "_params"
                and name != "operator"
                and type(getattr(operator, name)).__name__
                in [
                    "str",
                    "NoneType",
                    "TargetBandDescriptors",
                    "Aggregators",
                    "BinningOutputBands",
                    "BinningVariables",
                ]
            ]:
              
                if param in [
                    "targetBandDescriptors",
                    "aggregatorConfigs",
                    "variableConfigs",
                    "bandConfigurations",
                    "postProcessorConfig",
                    "productCustomizerConfig",
                ]:
                  
                    if param in ["bandConfigurations",
                                 "variableConfigs",
                                 "postProcessorConfig",
                                 "productCustomizerConfig"] and not getattr(operator, param):
                        continue

                    if (
                        isinstance(getattr(operator, param), TargetBandDescriptors)
                        or isinstance(getattr(operator, param), Aggregators)
                        or isinstance(getattr(operator, param), BinningOutputBands)
                        or isinstance(getattr(operator, param), BinningVariables)
                    ):
                        parameters_elem.append(getattr(operator, param).to_xml())

                    elif isinstance(getattr(operator, param), str):
                        parameters_elem.append(etree.fromstring(getattr(operator, param)))
                    else:
                        raise ValueError()

                else:
                    try: 
                        p_elem = self.root.xpath(xpath_expr + "/parameters/%s" % param)[0]

                        if getattr(operator, param) is not None:
                            if getattr(operator, param)[0] != "<":
                                p_elem.text = getattr(operator, param)
                            else:
                                if param != 'excludes':
                                    p_elem.text.append(etree.fromstring(getattr(operator, param)))
                    except IndexError:
                        pass

        else:

            node_elem = etree.SubElement(self.root, "node")
            operator_elem = etree.SubElement(node_elem, "operator")
            sources_elem = etree.SubElement(node_elem, "sources")
            
            src_prod_id = operator.get_src_product_id()
            if src_prod_id is None:
                src_prod_id = 'source'

            if isinstance(source, list):
                for index, s in enumerate(source):
                    if index == 0:
                        source_product_elem = etree.SubElement(sources_elem, src_prod_id)
                    else:
                        source_product_elem = etree.SubElement(sources_elem, "source.%s" % str(index))
                    source_product_elem.attrib["refid"] = s
            elif isinstance(source, dict):
                for key, value in source.iteritems():
                    source_product_elem = etree.SubElement(sources_elem, key)
                    source_product_elem.text = value
            elif source is not None:
                source_product_elem = etree.SubElement(sources_elem, src_prod_id)
                source_product_elem.attrib["refid"] = source

            parameters_elem = etree.SubElement(node_elem, "parameters")
            parameters_elem.attrib["class"] = "com.bc.ceres.binding.dom.XppDomElement"

            for param in [
                name
                for name in dir(operator)
                if name[:2] != "__"
                and name[-2:] != "__"
                and name != "_params"
                and name != "operator"
                and type(getattr(operator, name)).__name__
                in [
                    "str",
                    "NoneType",
                    "TargetBandDescriptors",
                    "Aggregators",
                    "BinningOutputBands",
                    "BinningVariables",
                ]
            ]:

                if param in [
                    "targetBandDescriptors",
                    "aggregatorConfigs",
                    "variableConfigs",
                    "bandConfigurations",
                    "postProcessorConfig",
                    "productCustomizerConfig",
                ]:
                    print(param, getattr(operator, param))
                    if param in ["bandConfigurations",
                                 "variableConfigs",
                                 "postProcessorConfig",
                                 "productCustomizerConfig"] and not getattr(operator, param): continue

                    print('Instance TargetBandDescriptors: ' + str(isinstance(getattr(operator, param), TargetBandDescriptors)))
                    print('Instance Aggregators: ' + str(isinstance(getattr(operator, param), Aggregators)))
                    print('Instance BinningOutputBands: ' + str(isinstance(getattr(operator, param), BinningOutputBands)))
                    print('Instance BinningVariables: ' + str(isinstance(getattr(operator, param), BinningVariables)))
                    print('Instance str: ' + str(isinstance(getattr(operator, param), str)))
                   
                    if (
                        isinstance(getattr(operator, param), TargetBandDescriptors)
                        or isinstance(getattr(operator, param), Aggregators)
                        or isinstance(getattr(operator, param), BinningOutputBands)
                        or isinstance(getattr(operator, param), BinningVariables)
                    ):
                        parameters_elem.append(getattr(operator, param).to_xml())
                    elif isinstance(getattr(operator, param), str):
                        parameters_elem.append(etree.fromstring(getattr(operator, param)))
                    else:

                        raise ValueError()

                else:
                    parameter_elem = etree.SubElement(parameters_elem, param)
                    if getattr(operator, param) is not None:
                        # special case: 'Merge' operator which has nested parameters 'excludes' and 'includes'
                        if operator.operator == 'Merge' and (param == 'excludes' or param == 'includes'):
                            self.append_merge_op_nested_param(operator, parameter_elem, param)
                        else:
                            if getattr(operator, param)[0] != "<":
                                parameter_elem.text = getattr(operator, param)
                            else:
                                parameter_elem.append(
                                    etree.fromstring(getattr(operator, param))
                                )

        node_elem.attrib["id"] = node_id

        operator_elem.text = operator.operator
        

    def save_graph(self, filename):
        """This method saves the SNAP Graph

        Args:
            filename: XML filename with '.xml' extension

        Returns
            None.

        Raises:
            None.
        """
        
        with open(filename, "w") as file:
            file.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            #file.write(unescape(etree.tostring(self.root, pretty_print=True).decode()).replace("&amp;", "&"))
            file.write(unescape(etree.tostring(self.root, pretty_print=True).decode()))

    def run(self, gpt_options=None):
        """This method runs the SNAP Graph using gpt

        Args:
            gpt_options: list of options to pass to gpt. Defaults to ['-x', '-c', '1024M']

        Returns
            res: gpt exit code
            err: gpt stderr

        Raises:
            None.
        """

        if gpt_options is None:
            gpt_options = ["-x", "-c", "1024M"]

        def _run_command(command, **kwargs):
            process = subprocess.Popen(args=command, stdout=subprocess.PIPE, stderr=subprocess.PIPE,  **kwargs)
            # todo: this hangs in the while loop. Check why!
            #while True:
            #    output = process.stdout.readline()
            #    err = process.stderr.readline()
            #    return_code = process.poll()
            #    if output.decode() == "" and return_code is not None:
            #        break
            #    if output:
            #        print(output.strip().decode())
            #    if err:
            #        print(err.strip().decode())

            # this works, but output (i.e. gpt progress) is not written before subprocess finished. todo: try to improve
            out, err = process.communicate()
            if out:
                print ('standard output of subprocess: ')
                print (out.strip().decode())
            if err:
                print ('standard error of subprocess: ')
                print (err.strip().decode())

            return_code = process.poll()

            return return_code

        os.environ["LD_LIBRARY_PATH"] = "."

        print("Processing the graph, this may take a while. Please wait...")

        fd, path = tempfile.mkstemp()
        rc = None

        try:
            self.save_graph(filename=path)
            options = [self.gpt_path, *gpt_options, path]
            rc = _run_command(options)
        finally:
            try:
                os.remove(path)
            except:
                pass

        if rc != 0:
            raise Exception("Graph execution failed (exit code {})".format(rc))
        else:
            print("Processing finished successfully.")

        return rc
