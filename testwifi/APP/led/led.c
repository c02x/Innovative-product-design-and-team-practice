#include "led.h"

/*******************************************************************************
* 函 数 名         : LED_Init
* 函数功能		   : LED初始化函数
* 输    入         : 无
* 输    出         : 无
*******************************************************************************/
void LED_Init(void)
{
	GPIO_InitTypeDef GPIO_InitStructure;//定义结构体变量
	
	RCC_APB2PeriphClockCmd(LED1_PORT_RCC|LED2_PORT_RCC,ENABLE);
	
	GPIO_InitStructure.GPIO_Pin=LED1_PIN;  //选择你要设置的IO口
	GPIO_InitStructure.GPIO_Mode=GPIO_Mode_Out_PP;	 //设置推挽输出模式
	GPIO_InitStructure.GPIO_Speed=GPIO_Speed_50MHz;	  //设置传输速率
	GPIO_Init(LED1_PORT,&GPIO_InitStructure); 	   /* 初始化GPIO */
	GPIO_SetBits(LED1_PORT,LED1_PIN);   //将LED端口拉高，熄灭所有LED
	
	GPIO_InitStructure.GPIO_Pin=LED2_PIN;  //选择你要设置的IO口
	GPIO_Init(LED2_PORT,&GPIO_InitStructure); 	   /* 初始化GPIO */
	GPIO_SetBits(LED2_PORT,LED2_PIN);   //将LED端口拉高，熄灭所有LED
}

// void LED_ON(void) declaration
void LED_ON(void)
{
	GPIO_ResetBits(LED1_PORT, LED1_PIN); // 点亮LED1
	GPIO_ResetBits(LED2_PORT, LED2_PIN); // 点亮LED2
}
// void LED_OFF(void) declaration
void LED_OFF(void)
{
	GPIO_SetBits(LED1_PORT, LED1_PIN); // 熄灭LED1
	GPIO_SetBits(LED2_PORT, LED2_PIN); // 熄灭LED2
}

bool LED1_Status(void)
{
   // 检查LED1状态
	return GPIO_ReadOutputDataBit(LED1_PORT, LED1_PIN) == Bit_RESET; // 如果是低电平，表示LED1亮
}

bool LED2_Status(void)
{
	// 检查LED2状态
	return GPIO_ReadOutputDataBit(LED2_PORT, LED2_PIN) == Bit_RESET; // 如果是低电平，表示LED2亮
   
}
