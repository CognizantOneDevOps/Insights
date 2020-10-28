
export function processData(props) {
    let jsonArrtoStr= [] as any;
    if(props.data.state == 'Done'){
      if (props.data.series.length > 0) {
        jsonArrtoStr = props.data.series[0].source;
      }
    }
    let googleChartData = {} as any;
    let uiResponseArr = [] as any;
    if (jsonArrtoStr.length > 0) {
        for (let i = 0; i < jsonArrtoStr.length; i++) {
            let arr = [] as any;
            let vectorMap = {} as any;
            vectorMap["vectorName"] = jsonArrtoStr[i]["heading"];
            let jsonObjtoStr = jsonArrtoStr[i];
            for (var vector in jsonObjtoStr["inferenceDetails"]) {
                let resultArray = [] as any;
                let data = jsonObjtoStr["inferenceDetails"][vector];
                let vectorProperty = {};
                googleChartData[data["kpiId"]] = data["resultSet"];
                vectorProperty["kpi"] = data["kpi"];
                vectorProperty["sentiment"] = data["sentiment"];
                vectorProperty["kpiId"] = data["kpiId"];
                vectorProperty["schedule"] = data["schedule"];
                vectorProperty["trendline"] = data["trendline"];
                vectorProperty["inference"] = data["inference"];
                vectorMap["lastRun"] = data["lastRun"];
                vectorMap["schedule"] = data["schedule"];
               
                if(data["resultSet"].length != undefined){
                    data.resultSet.forEach((x:any)=>{
                        return resultArray.push({ 'label': x.resultDate, 'value': x.value });
                    });
                }
                vectorProperty["resultSet"] = resultArray;
                if (data["sentiment"] == "POSITIVE" && data["trendline"] == "High to Low") {
                    vectorProperty["color"] = "green";
                    vectorProperty["type"] = "increased";
                    googleChartData[data["kpiId"]].push("green");
                }
                else if (data["sentiment"] == "POSITIVE" && data["trendline"] == "Low to High") {
                    vectorProperty["color"] = "green";
                    vectorProperty["type"] = "increased";

                    googleChartData[data["kpiId"]].push("green");
                }
                else if (data["sentiment"] == "NEGATIVE" && data["trendline"] == "Low to High") {
                    vectorProperty["color"] = "red";
                    vectorProperty["type"] = "increased";

                    googleChartData[data["kpiId"]].push("red");
                }
                else if (data["sentiment"] == "NEGATIVE" && data["trendline"] == "High to Low") {
                    vectorProperty["color"] = "red";
                    vectorProperty["type"] = "decreased";

                    googleChartData[data["kpiId"]].push("red");
                }
                else if (data["sentiment"] == "NEUTRAL") {
                    vectorProperty["color"] = "green";
                    vectorProperty["type"] = "same";

                    googleChartData[data["kpiId"]].push("green");
                }
                arr.push(vectorProperty);
            }
            vectorMap["data"] = arr;
            uiResponseArr.push(vectorMap);
        }
    }
    return uiResponseArr;
}

//Sample data to test inference without datasource
/*jsonArrtoStr = [{
  "heading": "CODEQUALITY",
  "inferenceDetails": [
    {
      "kpi": "Average Complexity",
      "sentiment": "POSITIVE",
      "trendline": "High to Low",
      "action": "AVERAGE",
      "inference": "Average Code Complexity has decreased to 110 from 136",
      "kpiId": 131,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:10 AM",
      "resultSet": [
        {
          "value": 128,
          "resultDate": "Jul 3, 2020 2:41:16 AM"
        },
        {
          "value": 136,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 110,
          "resultDate": "Jul 7, 2020 2:41:10 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Average Duplicated Blocks",
      "sentiment": "NEUTRAL",
      "trendline": "Low to High",
      "action": "AVERAGE",
      "inference": "Average duplicated blocks has remain to 1",
      "kpiId": 132,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:12 AM",
      "resultSet": [
        {
          "value": 1,
          "resultDate": "Jul 3, 2020 2:41:19 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 6, 2020 2:41:17 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 7, 2020 2:41:12 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Number of Quality Passed Blocks",
      "sentiment": "NEGATIVE",
      "trendline": "High to Low",
      "action": "COUNT",
      "inference": "Number of Quality Passed Blocks has decreased to 6 from 7",
      "kpiId": 133,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:13 AM",
      "resultSet": [
        {
          "value": 7,
          "resultDate": "Jul 3, 2020 2:41:20 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:19 AM"
        },
        {
          "value": 6,
          "resultDate": "Jul 7, 2020 2:41:13 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Number of Quality Failed Blocks",
      "sentiment": "NEGATIVE",
      "trendline": "Low to High",
      "action": "COUNT",
      "inference": "Number of Quality Failed Blocks has increased to 11 from 7",
      "kpiId": 134,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:11 AM",
      "resultSet": [
        {
          "value": 8,
          "resultDate": "Jul 3, 2020 2:41:17 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 11,
          "resultDate": "Jul 7, 2020 2:41:11 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Average Code Coverage",
      "sentiment": "NEUTRAL",
      "trendline": "Low to High",
      "action": "AVERAGE",
      "inference": "Average Code Coverage has remain same to 100",
      "kpiId": 135,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:12 AM",
      "resultSet": [
        {
          "value": 100,
          "resultDate": "Jul 3, 2020 2:41:19 AM"
        },
        {
          "value": 100,
          "resultDate": "Jul 6, 2020 2:41:18 AM"
        },
        {
          "value": 100,
          "resultDate": "Jul 7, 2020 2:41:12 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Number of Successful Sonar Executions",
      "sentiment": "NEGATIVE",
      "trendline": "High to Low",
      "action": "COUNT",
      "inference": "Number of Successful Sonar Executions has decreased to 6 from 7",
      "kpiId": 136,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:13 AM",
      "resultSet": [
        {
          "value": 7,
          "resultDate": "Jul 3, 2020 2:41:20 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:19 AM"
        },
        {
          "value": 6,
          "resultDate": "Jul 7, 2020 2:41:13 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Number of Failed Sonar Executions",
      "sentiment": "NEGATIVE",
      "trendline": "Low to High",
      "action": "COUNT",
      "inference": "Number of Failed Sonar Executions has increased to 5 from 1",
      "kpiId": 137,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:11 AM",
      "resultSet": [
        {
          "value": 2,
          "resultDate": "Jul 3, 2020 2:41:17 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 5,
          "resultDate": "Jul 7, 2020 2:41:11 AM"
        },
        "red"
      ]
    }
  ],
  "ranking": 1
}];*/