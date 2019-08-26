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
query the course Information 

Args:
	baseUrl : ipaddress or hostName
	courseId: courseId
Returns:
	courseInfo
'''
def queryCourse(baseUrl,courseId):
	website = '%s/studentCourse/%s/queryCourse' % (baseUrl, courseId)
	print('******enter queryCourse uri:%s ******' % (baseUrl))

	r = requests.get(url=website)   #带参数的GET请求
	r.encoding = 'utf-8' #这里添加一行
	#r.encoding = 'GBK'
	#response = json.loads(r.text)
	response = json_loads_byteified(r.text)
	#response=r.json()
	print(r.text)
	print('*****************')
	print(type(response['responseInfo']['title']))
	print((response['responseInfo']['title'].decode('utf8')))
	
	print(json.dumps(response['responseInfo']).decode('utf8'))
	
	for key,value in response['responseInfo'].items():
		if isinstance(value,str):
			response[key]=value.decode('utf8')
			print(response[key])
	print(json.dumps(response['responseInfo']))
	print('******leave queryCourse uri:%s ******' % (baseUrl))
	return response

def publishCourseToHot(baseUrl,course):
	website = '%s/hotCourseSearch/saveCourse' % (baseUrl)
	print(website)

	r = requests.post(url=website,json=course)   #带参数的GET请求
	r.encoding = 'utf-8' #这里添加一行
	#r.encoding = 'GBK'
	#response = json.loads(r.text)
	response = json_loads_byteified(r.text)
	#response=r.json()
	print(r.text)	
	return response


	
'''
to publish the new course to elasticsearch
'''

reload(sys)
sys.setdefaultencoding('utf8')
payload={
         "userId":"123456",
         "orderId":"0987654321",
         "category":"zuul",
         "status":0
         }
payload1={"clubId":"123456"}
'''
create the room
'''
baseUrl = "http://172.18.5.110:9003"
createRoomUrl=baseUrl+"/room/manage/create"
roomCreatePayload={
                   "clubId":"testLgq",
                   "roomId":"12345",
                   "roomName":"lgqroom",
                   "creatorUserId":"creatorUserId",
                   "creatorNickName":"nickName"
                  }
r=requests.post(url=createRoomUrl,json=roomCreatePayload)
print(r.text)
print('*****************query club**************')
queryroomUrl=baseUrl + "/room/manage/query"
r=requests.post(url=queryroomUrl,json=roomCreatePayload)
print(r.text)

