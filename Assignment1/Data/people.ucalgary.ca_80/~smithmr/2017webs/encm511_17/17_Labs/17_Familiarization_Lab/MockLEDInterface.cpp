/*
 * MockLEDInterface.cpp
 *
 *  Created on: Sep 11, 2017
 *      Author: smithmr
 */

#include <stdio.h>
#include <stdlib.h>
#include "MockLEDInterface.h"

static void DisplayMockcurrentLEDvalue(void);

static LEDBITS currentLEDvalue = GARBAGELEDRESULT;
static unsigned int mockLEDInterfaceInitialized = 0;
static LEDBITS mockLEDdisplayMask= (1 << (MOCKLEDDISPLAYSIZE - 1)) - 1;
static bool LEDDisplayOn = true;

void Init_LED_GPIOInterface(void) {
	mockLEDInterfaceInitialized = 1;
	currentLEDvalue = rand() & mockLEDdisplayMask;
	printf("mockLEDInterfaceInitialized\n");
	DisplayMockLEDValues(true);
}

void Write_LED_GPIOInterface(LEDBITS LEDBitPattern) {
	if (!mockLEDInterfaceInitialized) {
		printf("Can't do that yet with Write_LED_GPIOInterface\n");
		return;
	}
	currentLEDvalue = LEDBitPattern & mockLEDdisplayMask;
	DisplayMockcurrentLEDvalue();
}

unsigned char Read_LED_GPIOInterface(void){
	if (!mockLEDInterfaceInitialized) {
		printf("Can't do that yet with Read_LED_GPIOInterface\n");
		return rand() & mockLEDdisplayMask;
	}
	return currentLEDvalue;
}

static void DisplayMockcurrentLEDvalue(void) {
	if (!LEDDisplayOn) return;
	printf("Mock LED Display 0x%x b ", currentLEDvalue);
	for (unsigned int whichBit = MOCKLEDDISPLAYSIZE; whichBit > 0; whichBit--) {
		unsigned char bitPosition = (1 << (whichBit - 1) );
		if (currentLEDvalue &  bitPosition) printf("0");
		else printf("-");
	}
	printf("\n");
}

void DisplayMockLEDValues(bool showValues) {
	LEDDisplayOn = showValues;
}
