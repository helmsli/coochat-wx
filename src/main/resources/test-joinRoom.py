#!/usr/bin/env python
#-*-coding:utf-8-*-
import requests
import json 
import sys
import locale
import os
import codecs
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


'''
to publish the new course to elasticsearch
'''

reload(sys)
sys.setdefaultencoding('utf8')
'''
create the room
'''
baseUrl = "http://172.18.5.110:9003"
createRoomUrl=baseUrl+"/game/12345/join"
roomCreatePayload={
                   "clubId":"testLgq",
                   "roomId":"12345",
                   "userId":"67890",
                   "nickName":"lgqnick",
                   "avatar":"avatar",
                   "creatorNickName":"nickName"
                  }
r=requests.post(url=createRoomUrl,json=roomCreatePayload)
print(r.text)

print("*********************seat***************")
playerSeatUrl=baseUrl+"/game/12345/seat"
roomCreatePayload={
                   "clubId":"testLgq",
                   "roomId":"12345",
                   "userId":"67887",
                   "nickName":"lgqnick",
                   "avatar":"avatar",
                   "creatorNickName":"nickName",
                   "seatId":"7"
                  }
r=requests.post(url=playerSeatUrl,json=roomCreatePayload)
print(r.text)
playerQueryUrl=baseUrl+"/game/12345/query"
print("******************query user status ***************")
r=requests.post(url=playerQueryUrl,json=roomCreatePayload)
print(r.text)
