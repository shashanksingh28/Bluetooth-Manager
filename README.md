# Bluetooth-Manager 
An android application that implements an ad-hoc routing algorithm using bluetooth hardware in devices.

Consists of a routing service that enables each device to act as a node for every other known device.
Sender will broadcast a route-request packet to all known nodes within its bluetooth range and they will propogate the same further.
Once the destination node receives the packet, it will send a route-response back to the route-request with least hops.
This reoute-response will be used by nodes to update a temporary route table that completes the chain.

The application can be used to send files and chat messages from a user interface perspective.
