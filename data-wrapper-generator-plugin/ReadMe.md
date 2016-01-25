# Data Wrapper Plugin

This project is a maven plugin
It generates strongly typed *wrappers* around vert.x JsonObjects using example JSON. 
It really should really comply to json-schema but:

1. The schema is relatively complex and perhaps more so than what we need for spikes/poc.

2. There is a distinct lack of good quality and flexible third-party libraries that can be integrated with JsonObject 
efficiently.
