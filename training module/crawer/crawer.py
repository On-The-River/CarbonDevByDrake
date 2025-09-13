from os import path as path
from datetime import datetime
import requests, time, csv
from bs4 import BeautifulSoup

def in_one_day(file_path: str) -> bool:
    modified_timestamp = path.getmtime(file_path)
    modified_time = datetime.fromtimestamp(modified_timestamp)
    current_time = datetime.now()
    time_difference = (current_time - modified_time).total_seconds()
    one_day_seconds = 86400
    return time_difference <= one_day_seconds

root = 'D:/QST_Projs/Carbon/CarbonDevByDrake/training module/crawer'
blockTrading = root + '/blockTrading.csv'
marketConditions = root + '/marketConditions.csv'

if (path.exists(blockTrading)
    and path.exists(marketConditions)
    and in_one_day(blockTrading)
    and in_one_day(marketConditions)):
    exit(0)

url = "https://carbonmarket.cn/ets/cets/"

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Referer": "https://www.shex.com/"
}

try:
    response = requests.get(url, headers=headers, timeout=10)
    response.raise_for_status()
    response.encoding = "utf-8"

    soup = BeautifulSoup(response.text, "html.parser")
    text = soup.text

    start_mark = "全国碳市场(大宗交易)"
    start = text.find(start_mark)
    end = text.find("提示：用电脑浏览可看更多数据 数据来源声明", start)
    raw_data = (text[(start + len(start_mark)) : end]).strip()

    lines = [line.strip() for line in raw_data.split('\n') if line.strip()]
    header = lines[:5]
    records = lines[5:]

    # 按每条记录5个字段分组
    data = [records[i:i + 5] for i in range(0, len(records), 5)]

    # 写入CSV
    with open(blockTrading, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(header)
        writer.writerows(data)


    start_mark = "碳行情(全国碳市场) - CETS.cn"
    start = text.find(start_mark)
    end = text.find("提示：用电脑浏览可看更多数据 数据来源声明", start)
    rawData = (text[(start + len(start_mark)): end]).strip()

    lines = [line.strip() for line in rawData.split('\n') if line.strip()]
    header = lines[:9]
    records = lines[9:]

    data = [records[i:i + 9] for i in range(0, len(records), 9)]

    with open(marketConditions, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(header)
        writer.writerows(data)

except Exception as e:
    print(f"爬取失败：{str(e)}")

time.sleep(1)
