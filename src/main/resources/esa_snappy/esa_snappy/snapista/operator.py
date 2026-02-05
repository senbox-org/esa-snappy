from types import SimpleNamespace
from esa_snappy import GPF, jpy
from .operatorparams import OperatorParams


class Operator(SimpleNamespace):
    def __init__(self, operator, **kwargs):
        self.operator = operator
        self._params = {**OperatorParams(self.operator).params, **kwargs}

        return super().__init__(**self._params)

    def __str__(self):
        return "{}:\n\t{}".format(
            self.operator, "\n\t".join(["{}='{}'".format(key, value) for key, value in self.to_dict().items()])
        )

    def __repr__(self):
        return "Operator('{}', {})".format(
            self.operator, ", ".join(["{}='{}'".format(key, value) for key, value in self.to_dict().items()])
        )

    def to_dict(self):
        return dict([(name, getattr(self, name)) for name in list(self._params.keys())])

    def describe(self):
        """This function prints the human-readable information about a SNAP operator

        Args:

        Returns
            The human-readable information about the provided SNAP operator.

        Raises:
            None.
        """
        op_spi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(self.operator)

        print("Operator name: {}\n".format(op_spi.getOperatorDescriptor().getAlias()))
        print("Description: {}".format(op_spi.getOperatorDescriptor().getDescription()))
        print("Authors: {}\n".format(op_spi.getOperatorDescriptor().getAuthors()))
        print("{}".format(op_spi.getOperatorDescriptor().getName()))
        print("Version: {}\n".format(op_spi.getOperatorDescriptor().getVersion()))
        print("Sources:\n")
        source_desc = op_spi.getOperatorDescriptor().getSourceProductDescriptors()
        for src in source_desc:
            print('getSourceProductDescriptors src name: ' + src.getName())
            
        print("Parameters:\n")
        param_desc = op_spi.getOperatorDescriptor().getParameterDescriptors()

        for param in param_desc:
            print(
                "\t{}: {}\n\t\tDefault Value: {}\n".format(
                    param.getName(), param.getDescription(), param.getDefaultValue()
                )
            )

            if self.operator == "Write" and param.getName() == "formatName":
                print("\t\tPossible values: {}\n".format(self._get_formats("Write")))
            elif self.operator == "Read" and param.getName() == "formatName":
                print("\t\tPossible values: {}\n".format(self._get_formats("Read")))
            else:
                print("\t\tPossible values: {}\n".format(list(param.getValueSet())))
  
  
    def get_src_product_id(self):
        """This function returns the reference single source product identifier as specified in the Operator.
           It is not unique in SNAP, can be e.g. 'source', 'sourceProduct', 'l1BProduct' etc.

        Args:

        Returns
            The source product identifier as specified in the SNAP Operator.

        Raises:
            None.
        """
        op_spi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(self.operator)
        source_descr = op_spi.getOperatorDescriptor().getSourceProductDescriptors()   
        if len(source_descr) > 0:
            return source_descr[0].getName()
        else:
            return None
            
    def get_src_product_ids(self):
        """This function returns the source product identifiers as specified in the Operator.
           They are not unique in SNAP, can be e.g. 'source', 'sourceProduct', 'l1BProduct' etc.
           There can be more than one, e.g. for 'Collocate' operator: 'reference' and 'secondary'

        Args:

        Returns
            The source product identifier as specified in the SNAP Operator.

        Raises:
            None.
        """
        op_spi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(self.operator)
        source_descr = op_spi.getOperatorDescriptor().getSourceProductDescriptors()   
        if len(source_descr) > 0:
            src_descr_names = []
            for index, src in enumerate(source_descr):
                src_descr_names.append(src[index].getName())
            return src_descr_names
        else:
            return None
            

    @staticmethod
    def _get_formats(method):
        """This function provides a human-readable list of SNAP Read or Write operator formats.

        Args:
            None.

        Returns
            Human readable list of SNAP Write operator formats.

        Raises:
            None.
        """
        product_io_plug_in_manager = jpy.get_type("org.esa.snap.core.dataio.product_io_plug_in_manager")

        if method == "Read":
            plugins = product_io_plug_in_manager.getInstance().getAllReaderPlugIns()
        elif method == "Write":
            plugins = product_io_plug_in_manager.getInstance().getAllWriterPlugIns()
        else:
            raise ValueError

        formats = []

        while plugins.hasNext():
            plugin = plugins.next()
            formats.append(plugin.getFormatNames()[0])

        return formats
