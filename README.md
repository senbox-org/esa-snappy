esa-snappy
==========

[![pipeline status](https://gitlab.com/senbox-org/esa-snappy/badges/master/pipeline.svg)](https://gitlab.com/senbox-org/esa-snappy/-/commits/master)
[![coverage report](https://gitlab.com/senbox-org/esa-snappy/badges/master/coverage.svg)](https://gitlab.com/senbox-org/esa-snappy/-/commits/master)

The module *esa_snappy* enables Python developers to

1. use the SNAP Java API from Python, and to
2. extend SNAP by *operator plug-ins* for EO data processing written in the Python programming language.

It is worth mentioning that the *esa_snappy* module works with the standard *CPython*, so
that any native Python extension modules such as `numpy` and `scipy` can be used. 

The *esa_snappy* module
depends on a *bi-directional* Java-Python bridge *jpy* that enables calls from Python into a Java virtual machine
and, at the same time, the other way round. This bridge is implemented by the
[jpy Project](https://github.com/jpy-consortium/jpy) and is independent of the *esa_snappy* module.

Before you can start using the SNAP API or developing SNAP operator plugins with Python you
need to install *esa_snappy* in SNAP and configure SNAP for the desired Python version. 

The following documentation gives detailed instructions on how to proceed for using *esa_snappy*:

* [Installation/Configuration for SNAP 12+](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/3114106881/Installation+and+configuration+of+the+SNAP-Python+esa_snappy+interface+SNAP+version+12)
  **(in preparation)**
* [Installation/Configuration for SNAP 10/11](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/2499051521/Configure+Python+to+use+the+SNAP-Python+esa_snappy+interface+SNAP+version+10)
* [Installation/Configuration for SNAP <10](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/50855941/Configure+Python+to+use+the+SNAP-Python+snappy+interface+SNAP+versions+9)
* [How to use the SNAP API from Python](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/19300362/How+to+use+the+SNAP+API+from+Python)
* [How to integrate an operator](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/24051787/How+to+integrate+an+operator)
* [What to consider when writing an Operator in Python](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/42041346/What+to+consider+when+writing+an+Operator+in+Python)


