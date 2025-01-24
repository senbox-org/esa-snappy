#import attr
import lxml.etree as etree
from attrs import asdict, define, field


# @attr.s
@define
class AggregatorAvg(object):

    # type = attr.ib(init=False, default="AVG")
    # varName = attr.ib()
    # targetName = attr.ib()
    # weightCoeff = attr.ib(default=0.0)
    # outputCounts = attr.ib(default="true")
    # outputSums = attr.ib(default="true")

    type = field(init=False, default="AVG")
    var_name = field()
    target_name = field()
    weight_coeff = field(default=0.0)
    output_counts = field(default="true")
    output_sums = field(default="true")
    @output_counts.validator
    def _check_type(self, attribute, value):
        if value not in ["true", "false"]:
            raise ValueError("output_counts value is either 'true' or 'false'")

    @output_sums.validator
    def _check_type(self, attribute, value):
        if value not in ["true", "false"]:
            raise ValueError("output_sums value is either 'true' or 'false'")

    def __str__(self):

        self.type = "AVG"

        return self.__repr__()

    def __repr__(self):

        self.type = "AVG"

        return "AggregatorAvg({})".format(
            ", ".join(
                ["{}='{}'".format(key, value) for key, value in self.to_dict().items()]
            )
        )

    def to_dict(self):

        self.type = "AVG"

        # return attr.asdict(self)
        return asdict(self)

    def to_xml(self):

        self.type = "AVG"

        root = etree.Element("aggregator")

        for key, value in self.to_dict().items():

            elem = etree.SubElement(root, key)

            elem.text = str(value)

        return root
