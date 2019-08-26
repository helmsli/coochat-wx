#!/usr/bin/env python
#-*-coding:utf-8-*-
import requests
import json 
import sys
import locale
import os
import codecs
from loggerUtils import Logger
def _byteify(data, ignore_dicts = False):
    # if this is a unicode string, return its string representation
    if isinstance(data, unicode):
        return data.encode('utf-8')
    # if this is a list of values, return list of byteified values
    if isinstance(data, list):
        return [ _byteify(item, ignore_dicts=True) for item in data ]
    # if this is a dictionary, return dictionary of byteified keys and values
    # but only if we haven't already byteified it
    if isinstance(data, dict) and not ignore_dicts:
        return {
            _byteify(key, ignore_dicts=True): _byteify(value, ignore_dicts=True)
            for key, value in data.iteritems()
        }
    # if it's anything else, return it in its original form
    return data
def json_loads_byteified(json_text):
    return _byteify(
        json.loads(json_text, object_hook=_byteify),
        ignore_dicts=True
    )
def queryPlayerStatus():
    baseUrl = "http://172.18.5.110:9003"
    log.debug("******************query user status *seatId=5,,userId=67891**************")
    roomStartPayload={
                       "clubId":"testLgq",
                       "roomId":"1532676324347",
                       "userId":"67891",
                       "nickName":"lgqnick",
                       "avatar":"avatar",
                       "creatorNickName":"nickName",
                       "seatId":"5"
                      }
    
    playerQueryUrl=baseUrl+"/game/12345/query"
    r=requests.post(url=playerQueryUrl,json=roomStartPayload)
    log.debug(r.text)
    
    log.debug("******************query user status *seatId=6,,userId=67892**************")
    roomStartPayload={
                       "clubId":"testLgq",
                       "roomId":"1532676324347",
                       "userId":"67892",
                       "nickName":"lgqnick",
                       "avatar":"avatar",
                       "creatorNickName":"nickName",
                       "seatId":"6"
                      }
    
    playerQueryUrl=baseUrl+"/game/12345/query"
    r=requests.post(url=playerQueryUrl,json=roomStartPayload)
    log.debug(r.text)
    log.debug("******************query user status *seatId=1,,userId=67893**************")
    roomStartPayload={
                       "clubId":"testLgq",
                       "roomId":"1532676324347",
                       "userId":"67893",
                       "nickName":"lgqnick",
                       "avatar":"avatar",
                       "creatorNickName":"nickName",
                       "seatId":"1"
                      }
    
    playerQueryUrl=baseUrl+"/game/12345/query"
    r=requests.post(url=playerQueryUrl,json=roomStartPayload)
    log.debug(r.text)
    log.debug("******************query user status *seatId=7,,userId=67887**************")
    roomStartPayload={
                       "clubId":"testLgq",
                       "roomId":"1532676324347",
                       "userId":"67887",
                       "nickName":"lgqnick",
                       "avatar":"avatar",
                       "creatorNickName":"nickName",
                       "seatId":"7"
                      }
    
    playerQueryUrl=baseUrl+"/game/12345/query"
    r=requests.post(url=playerQueryUrl,json=roomStartPayload)
    log.debug(r.text)
    log.debug("******************query user status *seatId=2,userId=67882*************")
    roomStartPayload={
                       "clubId":"testLgq",
                       "roomId":"1532676324347",
                       "userId":"67882",
                       "nickName":"lgqnick",
                       "avatar":"avatar",
                       "creatorNickName":"nickName",
                       "seatId":"2"
                      }
    
    playerQueryUrl=baseUrl+"/game/12345/query"
    r=requests.post(url=playerQueryUrl,json=roomStartPayload)
    log.debug(r.text)
'''
end query
'''



'''
to publish the new course to elasticsearch
'''

reload(sys)
sys.setdefaultencoding('utf8')
log = Logger('startGame.log');
log.debug(("start program ********************************************"));
'''
create the room
'''
baseUrl = "http://172.18.5.110:9003"
roomStartPayload={
                   "clubId":"testLgq",
                   "roomId":"1532676324347",
                   "userId":"67890",
                   "nickName":"lgqnick",
                   "avatar":"avatar",
                   "creatorNickName":"nickName",
                   "seatId":"7"
                  }
playerQueryUrl=baseUrl+"/game/12345/reset"
log.debug("******************reset game ***************")
r=requests.post(url=playerQueryUrl,json=roomStartPayload)

playerQueryUrl=baseUrl+"/game/12345/start"
log.debug("******************start game ***************")
r=requests.post(url=playerQueryUrl,json=roomStartPayload)
log.debug(r.text)
response = json_loads_byteified(r.text)
if response['retCode'] !=0:
    log.debug("start game error:")
    exit()
gameSeqno=response['retMsg']
roomStartPayload['seqNo']=gameSeqno
queryPlayerStatus()

