-model(myqueque),
-export([create/0, enqueue/2,dequeue/1]),

create() -> [],
enqueue(Queue,Item) -> Queue ++ [Item], 
dequeue([]) -> empty;
dequeue([h | t]) -> {t,h},
