# Restaurant Management Platform (rmp).

## What is RMP?

A basic demo project on restaurant management platform. 

Functionalities that are added: 
* **Managing Food Recipies**
* **Manging Food Menu**
* **Managing Tables in Restaurant**
* **Managing Food Cart**
* **Bill Generation**
* **Updating Payment Status**
* **Notification triggers**. 

## Few words about the implementation style
Daemon threads with Java ExecutorService and Queues has been used for processing asynchrounous tasks. Services are written in decoupled manner, so that, they could be integrated as an independant microservices without much change in existing code.

A bsic screen stucture would look somehting like this -> [link](https://viewer.diagrams.net/index.html?tags=%7B%7D&highlight=0000ff&edit=_blank&layers=1&nav=1&title=rmp_front_end_screens.drawio#Uhttps%3A%2F%2Fraw.githubusercontent.com%2FAtypical3991%2Fdraw.io%2Fmain%2Frmp_front_end_screens.drawio#%7B%22pageId%22%3A%22yTE_MTdcGd8XoMPJyux-%22%7D)


