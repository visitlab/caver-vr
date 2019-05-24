# CAVER VR

This project aims to provide virtual reality user interface for CAVER Analyst (www.caver.cz).

The code is organized as a NetBeans module suite (http://wiki.netbeans.org/DevFaqSuitesVsClusters) as it enables the integration with CAVER Analyst.
CAVER Analyst is a NetBeans platform application (https://netbeans.org/features/platform/).
However, in contrast to this project CAVER Analyst is a closed source project.
Therefore, CAVER VR cannot be integrated with CAVER Analyst directly, but it is integrated with CAVER Analyst's binary distrubtion instead.

## Building

This project is a NetBeans module suite and it can be built using NetBeans 7.3.1 or later (https://netbeans.apache.org/).

Prerequisities:
* Binary distribution of CAVER Analyst 2.0 BETA 2 or later (https://www.caver.cz/index.php?sid=199).

Steps:
1. Open CAVER VR suite using NetBeans.
1. Edit `Important Files/Netbeans Platform Config` such that the `cluster.path` points to `caver_analyst` and `platform` subfolders of the CAVER Analyst binary distribution.
1. Build the CAVER VR suite using `CAVER VR -> Build`.

## Running

In order to run this project within a CAVER Analyst binary distribution it needs to be added among its `cluster`s.

Steps:
1. Copy and rename `{$PROJECT_DIR}/build/cluster` to `{$CAVER_ANALYST_DIR}/caver_vr`
1. Edit `{$CAVER_ANALYST_DIR}/caver_analyst.clusters` by adding a new row containing `caver_vr`
1. Run CAVER Analyst as usual.