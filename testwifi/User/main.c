#include "system.h"
#include "SysTick.h"
#include "led.h"         // 添加LED函数声明头文件
#include "usart.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include <string.h>
#include "cJSON.h"
#include "dht11.h"




// 声明外部变量
extern struct STRUCT_USARTx_Fram strEsp8266_Fram_Record;

// 函数声明
void ProcessWiFiData(uint8_t *data, uint16_t length);
void HandleMQTTMessage(const char *topic, const char *message, const char *message_le);

// 全局变量
char topic_id[28] = "mqtt_dev/demo1/report";
char topic_id1[28] = "mqtt_dev/demo1/report1";
char sub_topic_id[20] = "topic108";
uint8_t wifi_rx_buffer[RX_BUF_MAX_LEN]; // 用于暂存接收数据的缓冲区
//delcare global variable for if opening and closing read data
bool read_data_flag = true; // 用于控制是否读取数据
// ========== 设备处理结构体定义 ==========
typedef enum {
    DEVICE_LED,
    DEVICE_MOTOR,
    DEVICE_UNKNOWN
} DeviceType;

typedef struct {
    const char *device_name;        // 设备名称（JSON键名）
    DeviceType device_type;         // 设备类型枚举
    void (*handler)(const char *state); // 处理函数指针
} DeviceHandler;



void data_pros()	//数据处理函数
{
	u8 temp;  	    
	u8 humi;
	u8 temp_buf[3],humi_buf[3];
	DHT11_Read_Data(&temp,&humi);
	temp_buf[0]=temp/10+0x30;	
	temp_buf[1]=temp%10+0x30;
	temp_buf[2]='\0';
	//LCD_ShowString(55,100,tftlcd_data.width,tftlcd_data.height,16,temp_buf);
		
	humi_buf[0]=humi/10+0x30;	
	humi_buf[1]=humi%10+0x30;
	humi_buf[2]='\0';
	//LCD_ShowString(55,130,tftlcd_data.width,tftlcd_data.height,16,humi_buf);
	printf("temperature=%d°C  humidity=%d%%RH\r\n",temp,humi);
   // publish data to MQTT topic
    if (ESP8266_MQTT_Public_key_Value(topic_id, "temperature", temp_buf)) {
        printf("Published temperature successfully!\r\n");
    } else {
        printf("Failed to publish temperature!\r\n");
    }
    
    if (ESP8266_MQTT_Public_key_Value(topic_id1, "humidity", humi_buf)) {
        printf("Published humidity successfully!\r\n");
    } else {
        printf("Failed to publish humidity!\r\n");
    }

}


// ========== 设备处理函数实现 ==========
// LED处理函数（假设LED_Init()已在led.h中声明）
void LedHandler(const char *state) {
    if (strcmp(state, "on") == 0) {
        LED_ON(); // 假设LED_ON()在led.c中实现
        printf("Led turned on\r\n");
    } else if (strcmp(state, "off") == 0) {
        LED_OFF(); // 假设LED_OFF()在led.c中实现
        printf("Led turned off\r\n");
    } else {
        printf("Invalid state for LED: %s\r\n", state);
    }
}

// Motor处理函数
void MotorHandler(const char *state) {
    if (strcmp(state, "on") == 0) {
        // 这里添加电机启动代码
        TIM_SetCompare2(TIM3,400); // 假设使用TIM3通道2控制电机 ，让
        printf("Motor started\r\n");
    } else if (strcmp(state, "off") == 0) {
        // 这里添加电机关闭代码
        TIM_SetCompare2(TIM3,0); // 假设使用TIM3通道2控制电机，停止电机
        printf("Motor stopped\r\n");
    } else {
        printf("Invalid state for Motor: %s\r\n", state);
    }
}

// 设备处理注册表（可扩展）
DeviceHandler device_handlers[] = {
    {"Led", DEVICE_LED, LedHandler},
    {"Motor", DEVICE_MOTOR, MotorHandler},
    {NULL, DEVICE_UNKNOWN, NULL} // 结束标志
};

// ========== JSON解析函数扩展 ==========
void parse_json_message(const char *message) {
    cJSON *root;
    cJSON *item; // 修正变量名，避免未使用警告

    if (message == NULL) {
        printf("错误:传入的JSON字符串为空\n");
        return;
    }

    root = cJSON_Parse(message);
    if (!root) {
        printf("JSON格式错误:%s\n", cJSON_GetErrorPtr());
        return;
    }

    // 遍历JSON对象中的键值对（修正变量声明位置）
    item = root->child; // 变量声明已在代码块开头，此处直接使用
    while (item) {
        const char *device_name = item->string;
        const char *state = item->valuestring;
        
        // 查找设备处理器
        DeviceHandler *handler = device_handlers;
        while (handler->device_name) {
            if (strcmp(device_name, handler->device_name) == 0) {
                handler->handler(state);
                break;
            }
            handler++;
        }

        if (!handler->device_name) { // 未找到处理器
            printf("Unknown device: %s\r\n", device_name);
        }
        item = item->next;
    }

    cJSON_Delete(root); // 释放内存
}

int main() {
    // 初始化部分保持不变...
    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    USART1_Init(115200);
    LED_Init(); // 假设LED初始化函数在led.c中
    // Motor_Init(); // 如有电机初始化需添加

    WiFi_Config();

    printf("Hello, world!\r\n");
    printf("\r\n====================================\r\n");
    printf("STM32 + ESP8266 JSON Command Handler\r\n");
    printf("Supported devices: Led, Motor\r\n");
    printf("====================================\r\n");

    // 初始化WiFi和MQTT
    ESP8266_STA_TCP_Client_MQTT();
    if (ESP8266_Set_MQTT_Sub(sub_topic_id, "0")) {
        printf("\r\nSubscribed to MQTT topic successfully!\r\n");
    } else {
        printf("\r\nFailed to subscribe to MQTT topic!\r\n");
    }

    if (ESP8266_MQTT_Public_key_Value(topic_id, "aa", "01")) {
        printf("\r\nPublished MQTT message successfully!\r\n");
    } else {
        printf("\r\nFailed to publish MQTT message!\r\n");
    }


	while(DHT11_Init())	//检测DS18B20是否纯在
	{
		//LCD_ShowString(130,50,tftlcd_data.width,tftlcd_data.height,16,"Error   ");
		printf("DHT11 Check Error!\r\n");
		delay_ms(500);		
	}
	//LCD_ShowString(130,50,tftlcd_data.width,tftlcd_data.height,16,"Success");
	printf("DHT11 Check OK!\r\n");
    TIM3_CH2_PWM_Init(500,72-1); //频率是2Kh
	TIM_SetCompare2(TIM3,0);   //PWM输出初始值为0  电机应该是停止状态


    while (1) {
        // 处理WiFi数据
        if (strEsp8266_Fram_Record.InfBit.FramFinishFlag) {
            uint16_t data_len; // 声明在代码块开头
            __disable_irq();   // 进入临界区
            data_len = strEsp8266_Fram_Record.InfBit.FramLength;

            // 复制数据到本地缓冲区
            memcpy(wifi_rx_buffer, strEsp8266_Fram_Record.Data_RX_BUF, data_len);
            wifi_rx_buffer[data_len] = '\0'; // 添加字符串结束符

            // 清除标志和计数器
            strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
            strEsp8266_Fram_Record.InfBit.FramLength = 0;
            __enable_irq(); // 退出临界区

            // 处理接收到的数据
            ProcessWiFiData(wifi_rx_buffer, data_len);
        }
        // 其他业务逻辑...
        //温湿度
        if(read_data_flag) { // 如果开启读取数据标志
            printf("\r\nReading DHT11 data...\r\n");
        	data_pros();  	 //读取一次DHT11数据最少要大于100m
        } 

        delay_ms(200);
    }
    // 添加换行符避免警告
    printf("\n");
}

// 处理WiFi数据的函数
void ProcessWiFiData(uint8_t *data, uint16_t length) {
    // 示例：直接打印原始数据
    printf("[RAW DATA] %s\r\n", data);

    // 检查是否是MQTT消息
    if (strstr((char *)data, "+MQTTSUBRECV:") != NULL) {
        // 解析MQTT消息
        char *topic_start = strchr((char *)data, ',') + 1;
        char *msg_length_start = strchr(topic_start, ',') + 1;
        char *msg_start = strchr(msg_length_start, ',') + 1;

        // 提取主题和消息
        char topic[32] = {0};
        char msg_length[32] = {0};
        char message[128] = {0};

        sscanf(topic_start, "%[^,]", topic);
        sscanf(msg_length_start, "%[^,]", msg_length);
        sscanf(msg_start, "%[^,]", message);
        HandleMQTTMessage(topic, message, msg_length);
    }
    // 可以添加其他协议处理...
}

// 处理MQTT消息
void HandleMQTTMessage(const char *topic, const char *message, const char *message_length) {
    printf("Received MQTT message:\r\n");
    printf("Topic: %s\r\n", topic);         // topic_id
    printf("Message: %s\r\n\r\n", message); // json 字符串
    // todo: 根据实际需求处理消息
    parse_json_message(message); // 解析JSON消息
}
// 添加换行符避免警告