from attrs import asdict, define, field
import lxml.etree as etree


@define
class BinningBand(object):

    index = field()
    name = field()
    min_value = field()
    max_value = field()

    def __str__(self):

        return self.__repr__()

    def __repr__(self):

        return "BinningBand({})".format(
            ", ".join(
                ["{}='{}'".format(key, value) for key, value in self.to_dict().items()]
            )
        )

    def to_dict(self):

        return asdict(self)

    def to_xml(self):

        root = etree.Element("band")

        for key, value in self.to_dict().items():

            elem = etree.SubElement(root, key)

            elem.text = str(value)

        return root
