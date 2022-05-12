-module(login_manager).
-export([start/0,
         create_account/2,
         close_account/2,
         login/2,
         logout/1,
         online/0]).

start() -> 
    register (?MODULE,spawn(fun() -> loop(#{}) end)).

invoke(Request) -> 
    ?MODULE ! {Request,self()},
    receive {Res,?MODULE} -> Res end. 

create_account(User,Pass) -> invoke({create_account,User,Pass}).
close_account(User,Pass) -> invoke({close_account,User,Pass}).
login(User,Pass) -> invoke({login,User,Pass}).
online() -> invoke(online).

loop(Map) -> 
    receive
        {create_account,User,Pass,From} -> 
            case map:is_Key(User,Map) of 
                true -> 
                    From ! {user_exists,?MODULE},
                    loop(Map); 
                false -> 
                    From ! {ok,?MODULE},
                    loop(maps:put(User,{Pass, false},Map))
            end;
        {{close_account,User,Pass},From} -> 
            case maps:find(User,Map) of 
                {ok,{Pass, _}} -> 
                    From ! {ok,?MODULE},
                    loop(maps:remove(User,Map));
                    _-> 
                    From ! {invalid,?MODULE},
                    loop(MAP)
            end;
        {{login,User,Pass},From} -> 
            case maps:find(User,Map) of 
                 {ok,{Pass, _}} -> 
                    From ! {ok,?MODULE},
                    loop(maps:put(User,{Pass, true},Map))
                _-> 
                    From ! {invalid,?MODULE},
            end; 
        {{logout,User},From} -> 
            case maps:find(User,Map) of 
                 ok -> 
                    From ! {ok,?MODULE},
                    loop(maps:put(User,{Pass, false},Map))
                _-> 
                    From ! {invalid,?MODULE},
            end; 
        {online,From} -> 
            F = fun (User,{_Pass,true},Acc) -> [User | Acc]; 
                (_,_,Acc) -> Acc
            end, 
        From ! {fold(F,[],Map)}, ?MODULE},
    end.