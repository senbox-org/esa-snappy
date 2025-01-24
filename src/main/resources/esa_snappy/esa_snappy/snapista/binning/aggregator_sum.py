from attrs import asdict, define, field
import lxml.etree as etree


@define
class AggregatorSum(object):

    type = field(init=False, default="SUM")
    var_name = field()
    target_name = field()

    def __str__(self):

        self.type = "SUM"

        return self.__repr__()

    def __repr__(self):

        self.type = "SUM"

        return "AggregatorSum({})".format(
            ", ".join(
                ["{}='{}'".format(key, value) for key, value in self.to_dict().items()]
            )
        )

    def to_dict(self):

        self.type = "SUM"

        return asdict(self)

    def to_xml(self):

        self.type = "SUM"

        root = etree.Element("aggregator")

        for key, value in self.to_dict().items():

            elem = etree.SubElement(root, key)

            elem.text = str(value)

        return root
