# ESA_SNAPPY Package

This is the *esa_snappy* Python package. It provides the Python part of the SNAP Python support, which is
basically realized by the components:

- Java module for the configuration of the SNAP - Python interaction.
- Java-Python bridge *jpy* that enables calls from Python into a Java virtual machine.
  and, at the same time, the other way round. This bridge is implemented by the
  [jpy Project](https://deephaven.io/core/docs/how-to-guides/use-jpy/) and is independent of *esa_snappy*.
- **This package**, which provides:
  - Integration of *jpy* and configuration and initialization of the Java-Python bridge for usage with SNAP.
  - Extended support for using SNAP functionalities from Python, e.g. running chains of SNAP GPT operators from 
  xml graphs. Formerly hosted as *SNAPISTA* by [Terradue](https://www.terradue.com/), now integrated into the
    *esa_snappy* Python package.

## Further reading:

[Using SNAP in your Python programs](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/24051781/Using+SNAP+in+your+Python+programs)

[What to consider when writing an Operator in Python](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/42041346/What+to+consider+when+writing+an+Operator+in+Python)
