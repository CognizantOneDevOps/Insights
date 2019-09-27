#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
'''
Created on Jun 15, 2016

@author: 146414
'''
from requests.auth import HTTPBasicAuth
from requests_ntlm import HttpNtlmAuth
import requests
import logging
import json
import sys

class RestCommunicationFacade(object):
    headers = {"Accept":"application/json"}
    
    def __init__(self, sslVerify, responseType, enableValueArray):
        self.sslVerify = sslVerify
        self.responseType = responseType
        self.enableValueArray = enableValueArray
        
    def communicate(self, url, method, userName, password, data, authType='BASIC', reqHeaders=None, responseTupple=None, proxies=None):
        auth = None
        if(userName != None and password != None):
            if(authType == 'NTLM'):
                auth = HttpNtlmAuth(userName, password)
            else:
                auth = HTTPBasicAuth(userName, password)

        if reqHeaders == None:
            reqHeaders = RestCommunicationFacade.headers
                   
        response = None
        if('GET' == method):
            response = requests.get(url, auth=auth, headers=reqHeaders, data=data,proxies=proxies, verify=self.sslVerify)
        elif('POST' == method):
            response = requests.post(url, auth=auth, headers=reqHeaders, data=data,proxies=proxies, verify=self.sslVerify)
        else:
            raise ValueError('RestFacade: Unsupported HTTP Method '+method)
            
            
        if None == response:
            raise ValueError('RestFacade: Null response')
        elif 200 != response.status_code and 201 != response.status_code:
            raise ValueError('RestFacade: Unsupported response code '+str(response.status_code)+', url: '+url+', response received: '+response.content)    

        if responseTupple != None:
            responseTupple['headers'] = response.headers
            responseTupple['cookies'] = response.cookies
        if len(response.content) > 0:
            if self.responseType == 'JSON':
                return response.json()
            elif self.responseType == 'XML':
                return response.content
        else:
            return {}        
    
    def processResponse(self, template, response, injectData={}, useResponseTemplate=False):
        if template == None:
            raise ValueError('RestFacade: No parsingTemplate is provided')
        elif response == None:
            raise ValueError('RestFacade: No response is provided')
        else:
            pass
        
        dataArray = []
        if type(response) is list:
            for listItem in response:
                data = {};
                data.update(injectData)
                if useResponseTemplate:
                    self.parseResponse(template, listItem, data)
                else:                    
                    self.parseResponseWithXPath(listItem, data, None)
                dataArray.append(data)
        else:
            data = {};
            data.update(injectData)
            if useResponseTemplate:
                self.parseResponse(template, response, data)
            else:
                self.parseResponseWithXPath(response, data, None)
            dataArray.append(data)
        return dataArray
    
    def parseResponseWithXPath(self, responseObj, data, xpath):
        keyType = type(responseObj)
        if responseObj == None:
            return
        if keyType is dict:
            for key in responseObj:
                self.parseResponseWithXPath(responseObj.get(key, None), data, self.computeXPath(xpath, key))
        elif keyType is list:
            for index, item in enumerate(responseObj):
                self.parseResponseWithXPath(item, data, self.computeXPath(xpath, index))
        else:
            data[self.sanitizeKey(xpath)] = responseObj
    
    def sanitizeKey(self, key):
        return key.replace('/', '_')
    
    def computeXPath(self, xpath, key):
        newXpath = xpath
        if newXpath == None:
            newXpath = str(key)
        else:
            newXpath = xpath + '__' + str(key)
        return newXpath
    
    def parseResponse(self, templateObj, responseObj, data):
        keyType = type(templateObj)
        if responseObj == None:
            return
        dynamicName = None
        dynamicValue = None
        if keyType is dict:
            for key in templateObj:
                if '$' in key:
                    if '$name' in key:
                        dynamicName = responseObj.get(key.replace('$name$',''), None)
                    elif '$value' in key:
                        dynamicValue = responseObj.get(key.replace('$value$',''), None)
                else:
                    self.parseResponse(templateObj.get(key, None), responseObj.get(key, None), data)
        elif keyType is list:
            #Handle the multiple entries which are part of template object.
            for secTemplate in templateObj:
                secDataArray = []
                for item in responseObj:
                    if item:
                        secData = {};
                        self.parseResponse(secTemplate, item, secData)
                        if secData:
                            secDataArray.append(secData)
                for secData in secDataArray:
                    for secKey in secData:
                        prevValue = data.get(secKey, None)
                        if self.enableValueArray:
                            if prevValue and secData[secKey] not in prevValue:
                                prevValue.append(secData[secKey])
                            else:
                                data[secKey] = [secData[secKey]]
                        else:
                            if prevValue:
                                data[secKey] = prevValue + "," + secData[secKey]
                            else:
                                data[secKey] = secData[secKey]
        elif keyType is unicode or keyType is str:
            responseObjType = type(responseObj)
            if responseObjType is dict:
                logging.error('Dict object assignment to graph property is not allowed.')
                logging.error('Graph Property Name: '+templateObj)
                logging.error('Graph property value: '+json.dumps(responseObj))
                sys.exit() 
            else:
                data[templateObj] = responseObj
            
        else:
            raise ValueError('RestFacade: Unsupported data type found '+str(keyType))
        
        if dynamicName:
            data[dynamicName] = dynamicValue
