import requests
import json
import os
import sys
import csv
import time
import shutil
import datetime
from requests.auth import HTTPBasicAuth


'''
CONTENT OF neo4j_label_list.json
{
"PIVOTALTRACKER":["STORY"],
"GIT":["METADATA","DATA"],
"COMMIT":[""]
}
'''







def neo4j_export_json():
	file_name="neo4j_label_list.json"
	if os.name == 'nt':
		file_path="WINDOWS_PATH_OF_CORRESPONDING_FILE"+file_name
	else:
		file_path="/usr/INSIGHTS_HOME/"+file_name
	neo4j_label_category_data = json.load(open(file_path))
	neo4j_label_category_array=[]
	for key in neo4j_label_category_data:
		neo4j_label_category_array.append(key)

	#This one will export all the data before epoch time and put into csv file.
	
	neo4j_link=''
	neo4j_user=''
	neo4j_password=''
	
	#Determining the time and date of days ago
	try:
		days_ago_from_today=int(sys.argv[1])
		epoch_time_now = int(time.time())
		now = datetime.datetime.now()
	except:
		print "Please enter the number of days in argument e.g: to delete data older than 6 days: sudo python neo4j_export_json.py 6"
		exit(1)
	then = now - datetime.timedelta(days=days_ago_from_today)
	date_time = str(then).split(' ',1)[0]
	pattern = '%Y-%m-%d'
	epoch_times_ago = int(time.mktime(time.strptime(date_time, pattern)))
	#IN NEO4J QUERY WE HAVE TO PUT WHERE N.INSIGHTSTIME < epoch_times_ago SO BEFORE epoch_times_ago DAYS IT'LL DELETE THE DATA
	#Current timestamp
	ts = int(time.time())
	
	def process_node_in_csv(length_of_rows, data):
		print "Total Nodes in Neo4j " +length_of_rows
	get_data=neo4j_link+'/db/data/labels'
	try:
		response_label_list = requests.get(get_data, auth=HTTPBasicAuth(neo4j_user, neo4j_password), verify=False)
	except:
		print("Unexpected error:", sys.exc_info()[0])
		exit(1)
	
	ts = time.time()
	ts=str(ts)
	now = datetime.datetime.now()
	now=str(now)
	now=now.split(" ")[0]
	folder_name=str(now)+'_'+ts
	try:
		os.makedirs(folder_name)
	except OSError as e:
		if e.errno != errno.EEXIST:
			raise
	if os.name == 'nt':
		backup_path=os.getcwd()+'\\'+folder_name+'\\'
	else:
		backup_path=os.getcwd()+'/'+folder_name+'/'
	
	try:
		if response_label_list.status_code == 200:
			label_list=json.loads(response_label_list.text)
			for i in range(0, len(json.loads(response_label_list.text))):
				for label_name_from_file in neo4j_label_category_data:
					if label_list[i] == label_name_from_file:
						label_key_depth=''
						for label_key in neo4j_label_category_data.get(label_name_from_file, None):
							if label_key != '':
								label_key_depth=label_name_from_file+':'+label_key
							else:
								label_key_depth=label_name_from_file
							json_total_array=[]
							table_name_in_neo4j=label_key_depth
							flag=0
							csv_column_name=[]
							
							csv_column_name.append('neo4j_data_index_tag')
							
							csv_input_data=[]
							data_to_send={"query" : "MATCH (n:"+table_name_in_neo4j+") where n.inSightsTime < "+str(epoch_times_ago)+" return count(n)","params" : { }}
							get_data_url=neo4j_link+'/db/data/cypher'
							run_query_response_count = requests.post(get_data_url, auth=HTTPBasicAuth(neo4j_user, neo4j_password), verify=False, data=data_to_send)
							if run_query_response_count.status_code == 200:
								run_query_response_count=json.loads(run_query_response_count.text)
								length_of_received_data=run_query_response_count['data'][0][0]
								print 'Total nodes in Neo4j is : '+str(length_of_received_data)
								data_length_split=0

								#HOW MANY NODES IN A SINGLE API CALL WE WANT TO RECEIVE FROM NEO4J
								iteration_count=2000

								while (data_length_split < length_of_received_data):
									neo4j_query={"query" : "MATCH (n:"+table_name_in_neo4j+") where n.inSightsTime < "+str(epoch_times_ago)+" return n skip "+str(data_length_split)+" limit "+str(iteration_count),"params" : { }}
									get_data_url=neo4j_link+'/db/data/cypher'
									final_query_response = requests.post(get_data_url, auth=HTTPBasicAuth(neo4j_user, neo4j_password), verify=False, data=neo4j_query)
									final_query_response=json.loads(final_query_response.text)
									if length_of_received_data - data_length_split < iteration_count:
										data_length_split = data_length_split + length_of_received_data - data_length_split
									else:
										data_length_split = data_length_split + iteration_count
									run_query_response=final_query_response
									for j in range(0,len(run_query_response['data'])):
										each_node_json_obj={}
										table_data_node=run_query_response['data'][j][0]['data']
										#WE HAVE TO TAKE THE METADATA COMING FROM API AND ADD TO THE PRESENT JSON STRUCTURE SO THAT IN CSV ONE MORE FIELD WILL BE THERE REGARDING INDEXED DATA IN NEO4J
										
										table_metadata_node=run_query_response['data'][j][0]['metadata']['labels']
										each_node_metadata_string=''
										for length_table_metadata_node in range(0,len(table_metadata_node)):
											each_node_metadata_string = each_node_metadata_string + table_metadata_node[length_table_metadata_node] + ':'
										each_node_metadata_string = each_node_metadata_string[0:len(each_node_metadata_string)-1]
										
										for key in table_data_node:
											if key != 'uuid':
												int_flag=0
												each_node_json_obj[key]=table_data_node[key]
												each_node_json_obj['neo4j_data_index_tag']=each_node_metadata_string
										if len(each_node_json_obj) > 0:
											json_total_array.append(each_node_json_obj)
									time.sleep(1)
							file_name_of_json_file=table_name_in_neo4j+'.json'
							file_name_of_json_file=file_name_of_json_file.replace(":", "_")
							with open(os.path.join(backup_path, file_name_of_json_file), 'w') as outfile:
								json.dump(json_total_array, outfile)
								print "Total Count of nodes in Backup JSON "+file_name_of_json_file+" is : "+str(len(json_total_array))
								if (len(json_total_array) > 0):
										print "Data BackUp Successful for "+table_name_in_neo4j
								else:
									print "Zero Nodes. No BackUp Required for "+table_name_in_neo4j
	except:
		print("BackUp Failure!!!")
		print("Unexpected error:", sys.exc_info()[0])
if __name__ == "__main__":
    neo4j_export_json()
