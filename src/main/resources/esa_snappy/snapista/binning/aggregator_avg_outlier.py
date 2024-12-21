import lxml.etree as etree
from attrs import asdict, define, field


@define
class AggregatorAvgOutlier(object):

    type = field(init=False, default="AVG_OUTLIER")
    var_name = field()
    target_name = field()

    def __str__(self):

        self.type = "AVG_OUTLIER"

        return self.__repr__()

    def __repr__(self):

        self.type = "AVG_OUTLIER"

        return "AggregatorAvgOutlier({})".format(
            ", ".join(
                ["{}='{}'".format(key, value) for key, value in self.to_dict().items()]
            )
        )

    def to_dict(self):

        self.type = "AVG_OUTLIER"

        return asdict(self)

    def to_xml(self):

        self.type = "AVG_OUTLIER"

        root = etree.Element("aggregator")

        for key, value in self.to_dict().items():

            elem = etree.SubElement(root, key)

            elem.text = str(value)

        return root
