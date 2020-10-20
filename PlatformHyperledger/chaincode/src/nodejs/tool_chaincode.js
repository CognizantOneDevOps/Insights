/*
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
 */

'use strict';
const shim = require('fabric-shim')
const util = require('util')
var logger = shim.newLogger('tool_chaincode.js')

let Chaincode = class {
  async Init(stub) {
    let ret = stub.getFunctionAndParameters()
	logger.info('=========== Instantiated Traceability Chaincode ===========')
    logger.info(ret)
    return shim.success()
  }

  async Invoke(stub) {
    logger.debug('Transaction ID: ' + stub.getTxID());
    logger.debug(util.format('Args: %j', stub.getArgs()));

    let ret = stub.getFunctionAndParameters();
    logger.debug(ret);

    let method = this[ret.fcn]
    if (!method) {
      logger.warn('no function of name:' + ret.fcn + ' found')
      throw new Error('Received unknown function ' + ret.fcn + ' invocation')
    }
    try {
		let payload = await method(stub, ret.params, this);
		console.info("event payload: "+payload);
		//Adding event
		let sampleEvent = stub.setEvent("InstantiateSuccess", payload);
		if(sampleEvent!=null){
			logger.error('Error in Instantiate event..'+sampleEvent.Error());
			return shim.error(sampleEvent.Error());
		}
		else{
			logger.info('Instantiate event successful')
			return shim.success(payload);
		}
	} catch (err) {
	  logger.error(err)
	  return shim.error(err)
	}
  }

  // Instantiate - create a new baseAsset and secondary asset
  async Instantiate(stub, args, thisClass) {
	let argJson = JSON.parse(args[0])
	
	if(!argJson.hasOwnProperty('toolName') || !argJson.hasOwnProperty('timestamp')){
		let errMsg = await thisClass['generatePayload']('Missing Essential properties (eg-timestamp,toolName) in payload',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	
	let toolName = argJson.toolName
	let tool = {}
	//=== Take the asset model defined in datamodel ===
	delete require.cache[require.resolve('./datamodel.json')]
	let datamodel = require('./datamodel.json')
	
	if(datamodel.hasOwnProperty(toolName.toUpperCase()))
		tool = datamodel[toolName.toUpperCase()]
	else{
		let errMsg = await thisClass['generatePayload']("Unsupported toolname",103);
		return Buffer.from(JSON.stringify(errMsg))
	}
	
    // ==== Input sanitation ====
	if(argJson.timestamp=="" || argJson.toolName==""){
		let errMsg = await thisClass['generatePayload']('Empty Essential properties (eg-assetID,timestamp,toolName) in payload',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	
	if(!argJson.hasOwnProperty('date') || !/^((19|20)\d{2})-((0|1|)\d{1})-((0|1|2|3)\d{1})/.test(argJson.date)){
		let errMsg = await thisClass['generatePayload']('Incorrect/Empty *date* field: date field must be in yyyy-mm-dd format',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	
	//receive the arguments into meaningful variables
	let assetID
	let timestamp = argJson.timestamp
	let formattedDate = argJson.date
	
	let assetArray = []
	
	//assign values to the asset properties
	for(var properties in tool){
		if(/^.*AssetID/.test(properties))
			assetID = properties
		tool[properties] = argJson[properties]
	}
	//create the sprint asset
	if(toolName == 'JIRA' && argJson.hasOwnProperty('sprintNames'))
		await thisClass['CreateSprintAsset'](stub, [argJson.sprintNames, tool[assetID]], thisClass)
	
	if(tool[assetID]!="")
		logger.info(assetID+" "+timestamp+" "+formattedDate)
	else{
		let errMsg = await thisClass['generatePayload']('Missing '+assetID+' in payload',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	let updateFlag = 0
    
	try{
		assetID = tool[assetID]
		let assetState = await stub.getState(assetID)
		//check if asset exists
		if(assetState.toString()){
			assetState = JSON.parse(assetState)
			if(assetState.timestamp==timestamp){
				//to accomodate jira attachment changelogs
				let exp
				delete tool.date
				for(var props in tool){
					if(typeof(tool[props])=="object")
						exp = !await thisClass['isEqual'](tool[props], assetState[props])
					else
						exp = tool[props]!=assetState[props]
					if(exp){
						updateFlag = 1
						break
					}
				}
				if(updateFlag!=1){
					let errMsg = await thisClass['generatePayload']('The asset is already present',103)
					return Buffer.from(JSON.stringify(errMsg))
				}
			}else
				updateFlag = 1
		}
		else
			updateFlag = 0
		assetState = tool
		if(updateFlag==0){
			//check if date asset already exists
			let dateAsset = await stub.getState(formattedDate)
			if(dateAsset.toString()){//date asset already exists
				assetArray = JSON.parse(dateAsset).assetArray
				let isPresent = false
				for(var asset in assetArray){
					if(assetID==asset){
						isPresent=true
						break
					}
				}
				if(!isPresent){
					assetArray.push(assetID)
					updateFlag = 0//update both asset and dateAsset
				}
				else
					updateFlag = 1//update only asset
			}
			else{//date asset doesn't exist
				assetArray.push(assetID)
				updateFlag = 0
			}
		}
		//save both dateAsset & asset if they are new
		if(updateFlag==0){
			await stub.putState(assetID, Buffer.from(JSON.stringify(assetState)))
			await stub.putState(formattedDate, Buffer.from("{\"assetArray\":"+JSON.stringify(assetArray)+"}"))
		}
		//dateAsset exists & already has the assetID, updating the asset only
		else if(updateFlag==1)
			await stub.putState(assetID, Buffer.from(JSON.stringify(assetState)))
		let msg = await thisClass['generatePayload'](assetState,201)
		return Buffer.from(JSON.stringify(msg))
	}
	catch(err){
		let errMsg = await thisClass['generatePayload'](err.toString(),102)
		return Buffer.from(JSON.stringify(errMsg))
	}
  }
  
  async CreateSprintAsset(stub, args, thisClass){
	try{
		let sprintNames = args[0]
		if(typeof(sprintNames)!="object"){
			sprintNames = sprintNames.split(", ")
		}
		let almAssetID = args[1]
		let sprintAssetState
		for(var sprint in sprintNames){
			logger.info(sprintNames[sprint])
			sprintAssetState = await stub.getState(sprintNames[sprint])
			if(sprintAssetState.toString()){
				sprintAssetState = JSON.parse(sprintAssetState)
				if(!sprintAssetState.downlink.includes(almAssetID))
					sprintAssetState.downlink.push(almAssetID)
			}else{
				sprintAssetState = {"property": "SPRINT", "uplink": "null", "downlink": [almAssetID]}
			}
			logger.warn(sprintAssetState)
			await stub.putState(sprintNames[sprint], Buffer.from(JSON.stringify(sprintAssetState)))
		}
		return
	}catch(err){
		let errMsg = await thisClass['generatePayload'](err.toString(),102)
		return Buffer.from(JSON.stringify(errMsg))
	}
  }
  
  async isEqual(inputObj, ledgerObj){
	if(typeof(ledgerObj)!=typeof(inputObj))
		return false
	if(inputObj.length!=ledgerObj.length)
		return false
	else{
		for(var elem in inputObj){
			if(inputObj[elem]!=ledgerObj[elem])
				return false
		}
		return true
	}
  }
  
  // GetAssetDetails - read an asset from chaincode worldstate
  async GetAssetDetails(stub, args, thisClass) {
    if (args.length != 1) {
		let errMsg = await thisClass['generatePayload']('Incorrect number of arguments. Expecting assetID to query',103)
		return Buffer.from(JSON.stringify(errMsg))
    }

    let assetID = args[0]
    if (!assetID) {
		let errMsg = await thisClass['generatePayload']('assetID must not be empty',103)
		return Buffer.from(JSON.stringify(errMsg))
    }
	try{
		let msg
		let assetAsBytes = await thisClass['GetWorldState'](stub,[assetID],thisClass)
		let assetJson = JSON.parse(assetAsBytes)
		if(assetJson.hasOwnProperty("statusCode"))
			return assetAsBytes
		else{
			if(assetJson.property == "SPRINT"){
				let assetIDs = assetJson.downlink
				assetJson = []
				for(var id in assetIDs){
					assetJson.push(JSON.parse(await thisClass['GetWorldState'](stub,[assetIDs[id]],thisClass)))
				}
			}
			msg = await thisClass['generatePayload'](assetJson,200)
			return Buffer.from(JSON.stringify(msg))
		}
	}
	catch(err){
		let errMsg = await thisClass['generatePayload'](err.toString(),102)
		return Buffer.from(JSON.stringify(errMsg))
	}
  }

  //simple getState on stub
  async GetWorldState(stub, args, thisClass) {
	let assetID = args[0];
	let assetAsBytes = await stub.getState(assetID);
	if(!assetAsBytes.toString()){
		let errMsg = await thisClass['generatePayload']("no asset found for assetID: "+assetID,104)
		return Buffer.from(JSON.stringify(errMsg))
	}
	//this method is expected to be called within the chaincode only, hence successful payloads will not have statusCodes
	else
	  return Buffer.from(assetAsBytes)
  }

// getAllResults
// ==================================================
 //async getAllResults(iterator, isHistory) {
 async getAllResults(promiseOfIterator, isHistory) {
	
    let allResults = [];

    //while (true) {
			  for await (const res of promiseOfIterator) {
				if (res.value && res.value.toString()) {
					let jsonRes = {};
					logger.info(res.value.toString('utf8'));
					if (isHistory && isHistory === true) {
						logger.info("Sanitizing historical data..");
						jsonRes.TxId = res.txId;
						jsonRes.Timestamp = res.timestamp;
						if(res.is_delete)
						  jsonRes.IsDelete = res.is_delete.toString();
						try {
							if(res.value.toString('utf8')!='')
								jsonRes.Value = JSON.parse(res.value.toString('utf8'));
							else
								jsonRes.Value = res.value.toString('utf8')
						} catch (err) {
								logger.warn(err);
								jsonRes.Value = res.value.toString('utf8');
							}
					}
					else {
						jsonRes.Key = res.key;
						try {
				      if(res.value.toString('utf8')!='\u0000')
					      jsonRes.Record = JSON.parse(res.value.toString('utf8'));
				      else
					      jsonRes.Record = res.value.toString('utf8');
						} catch (err) {
								logger.warn(err+"");
								jsonRes.Record = res.value.toString('utf8');
							}
					}
					allResults.push(jsonRes);

				}
			}
				    logger.info('end of data');
					const res = await promiseOfIterator;
                    let iterator = res.iterator ? res.iterator : res;
                    await iterator.close();
					logger.info(allResults);
  }

// Get History for an tool asset
//=====================================================
async GetAssetHistory(stub, args, thisClass) {

    if (args.length != 1) {
		let errMsg = await thisClass['generatePayload']('Incorrect number of arguments. Expecting assetID',103)
		return Buffer.from(JSON.stringify(errMsg))
    }
    let assetID = args[0]

	let assetAsBytes = await stub.getState(assetID)
	if(!assetAsBytes.toString()){
		let errMsg = await thisClass['generatePayload']("no records found for assetID: "+assetID,104)
		return Buffer.from(JSON.stringify(errMsg))
	}
	else{
		let assetJson = JSON.parse(assetAsBytes)
		let assetSet = new Set()
		let arrayToReturn = []
		let resultsIterator
		let results
		assetSet = await thisClass['traverseUp'](stub,assetJson,assetSet,thisClass)
		assetSet.add(assetID)
		assetSet = await thisClass['traverseDown'](stub,assetJson,assetSet,thisClass)
		for(const id of assetSet){
			resultsIterator =  stub.getHistoryForKey(id)
			logger.info("after hethostroy")
			results = await thisClass['getAllResults'](resultsIterator, true)
			for (var r in results){
				delete results[r].Timestamp
				delete results[r].IsDelete
				arrayToReturn.push(results[r])
			}			
		}
		
		let msg = await thisClass['generatePayload'](arrayToReturn,200)
		return Buffer.from(JSON.stringify(msg))
	}
  }
  
  async traverseDown(stub, args, setToReturn, thisClass){
	let assetJson = args
	let resultsIterator
	let response
	let method = thisClass['getAllResults']
	if(assetJson.downlink!="null"){
		for(var fields in assetJson.downlink){
			if(typeof(assetJson.downlink[fields])=='string'){
				let queryString = '{\"selector\": {\"'+fields+'\": {\"$in\": [\"'+assetJson.downlink[fields]+'\"]}}}'
				response = await method( stub.getQueryResult(queryString),false)
				
				if(response.length > 0 && response[0].hasOwnProperty('Key')){
					for (var i in response){
						setToReturn.add(response[i].Key)
						setToReturn = await thisClass['traverseDown'](stub, response[i].Record, setToReturn, thisClass)
					}
				}else
					continue
			}
			else{
				let downlinkArr = assetJson.downlink[fields]
				for(var each in downlinkArr){
					let queryString = '{\"selector\": {\"'+fields+'\": {\"$in\": [\"'+downlinkArr[each]+'\"]}}}'
					response = await method( stub.getQueryResult(queryString),false)
					
					if(response.length > 0 && response[0].hasOwnProperty('Key')){
						for (var i in response){
							setToReturn.add(response[i].Key)
							setToReturn = await thisClass['traverseDown'](stub, response[i].Record, setToReturn, thisClass)
						}
					}else
						continue
				}
			}
		}
	}else
		return setToReturn
	return setToReturn
  }
  
  async traverseUp(stub, args, setToReturn, thisClass){
	let assetJson = args
	let resultsIterator
	let response
	let method = thisClass['getAllResults']
	if(assetJson.uplink!="null"){
		for(var fields in assetJson.uplink){
			if(typeof(assetJson.uplink[fields])=='string'){
				let queryString = '{\"selector\": {\"'+fields+'\": {\"$in\": [\"'+assetJson.uplink[fields]+'\"]}}}'
				response = await method( stub.getQueryResult(queryString),false)
				
				if(response.length > 0 && response[0].hasOwnProperty('Key')){
					for (var i in response){
						setToReturn.add(response[i].Key)
						setToReturn = await thisClass['traverseUp'](stub, response[i].Record, setToReturn, thisClass)
					}
				}else
					continue
			}
			else{
				let uplinkArr = assetJson.uplink[fields]
				for (var each in uplinkArr){
					let queryString = '{\"selector\": {\"'+fields+'\": {\"$in\": [\"'+uplinkArr[each]+'\"]}}}'
					response = await method( stub.getQueryResult(queryString),false)
					
					if(response.length > 0 && response[0].hasOwnProperty('Key')){
						for (var i in response){
							setToReturn.add(response[i].Key)
							setToReturn = await thisClass['traverseUp'](stub, response[i].Record, setToReturn, thisClass)
						}
					}else
						continue
				}
			}
		}
	}else
		return setToReturn
	return setToReturn
  }

  //get assetHistory based on given range of date
  async getAssetsByDate(stub, args, thisClass) {
	let model = require('./datamodel.json')
	if (args.length != 3){
		let errMsg = await thisClass['generatePayload']('Incorrect number of arguments. Expecting 3',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	else if(args.length==3 && (!/^((19|20)\d{2})-((0|1)\d{1})-((0|1|2|3)\d{1})/.test(args[0]) || !/^((19|20)\d{2})-((0|1)\d{1})-((0|1|2|3)\d{1})/.test(args[1]))){
		let errMsg = await thisClass['generatePayload']('Incorrect/Empty date format. Expected yyyy-mm-dd',103)
		return Buffer.from(JSON.stringify(errMsg))
	}else if(args[2]=="" || !model.hasOwnProperty(args[2])){
		let errMsg = await thisClass['generatePayload']('Incorrect/Empty toolname',103)
		return Buffer.from(JSON.stringify(errMsg))
	}
	try{
		let date = require('date-and-time')
		let method = thisClass['getAllResults']
		let startDate = args[0]
		let endDate = args[1]
		let toolName = args[2]
		let formattedEndDate = date.parse(endDate,'YYYY-MM-DD')
		formattedEndDate = date.addDays(formattedEndDate,1)
		endDate = date.format(formattedEndDate,'YYYY-MM-DD')
		let rangeResultIterator;
		rangeResultIterator =  stub.getStateByRange(startDate, endDate)
		let result = await method(rangeResultIterator, false)
		
		let key=[]
		let eachBaseDetails
		let getDetailsMethod = thisClass['GetWorldState']
		for(var i=0; i<result.length; i++){
			for(var id in result[i].Record.assetArray){
				let assetID = result[i].Record.assetArray[id]
				eachBaseDetails = JSON.parse(await getDetailsMethod(stub, [assetID], thisClass))
				if(eachBaseDetails.toolName==toolName)
					key.push(eachBaseDetails)
			}
		}
		let msg = await thisClass['generatePayload'](key,200)
		return Buffer.from(JSON.stringify(msg))
		
	}catch(err){
		let errMsg = await thisClass['generatePayload'](err.toString(),102)
		return Buffer.from(JSON.stringify(errMsg));
	}
  }

  async generatePayload(msg, statusCode){
	let errObj = {"statusCode": statusCode, "msg": msg};
	return errObj
  }
};


shim.start(new Chaincode());
