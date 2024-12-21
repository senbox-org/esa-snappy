from attrs import asdict, define, field
import lxml.etree as etree


@define
class AggregatorMinMax(object):

    type = field(init=False, default="MIN_MAX")
    var_name = field()
    target_name = field()

    def __str__(self):

        self.type = "MIN_MAX"

        return self.__repr__()

    def __repr__(self):

        self.type = "MIN_MAX"

        return "AggregatorMinMax({})".format(
            ", ".join(
                ["{}='{}'".format(key, value) for key, value in self.to_dict().items()]
            )
        )

    def to_dict(self):

        self.type = "MIN_MAX"

        return asdict(self)

    def to_xml(self):

        self.type = "MIN_MAX"

        root = etree.Element("aggregator")

        for key, value in self.to_dict().items():

            elem = etree.SubElement(root, key)

            elem.text = str(value)

        return root
