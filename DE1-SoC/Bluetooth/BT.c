/*
 * BT.c
 *
 *  Created on: Mar 30, 2021
 *      Author: wensh
 */

#include <stdio.h>
#include <math.h>

// addresses for the bluetooth serial UART
#define BT_ReceiverFifo 					(*(volatile unsigned char *)(0xFF210220))
#define BT_TransmitterFifo 					(*(volatile unsigned char *)(0xFF210220))
#define BT_InterruptEnableReg 				(*(volatile unsigned char *)(0xFF210222))
#define BT_InterruptIdentificationReg 		(*(volatile unsigned char *)(0xFF210224))
#define BT_FifoControlReg 					(*(volatile unsigned char *)(0xFF210224))
#define BT_LineControlReg 					(*(volatile unsigned char *)(0xFF210226))
#define BT_ModemControlReg 					(*(volatile unsigned char *)(0xFF210228))
#define BT_LineStatusReg 					(*(volatile unsigned char *)(0xFF21022A))
#define BT_ModemStatusReg 					(*(volatile unsigned char *)(0xFF21022C))
#define BT_ScratchReg 						(*(volatile unsigned char *)(0xFF21022E))
#define BT_DivisorLatchLSB 					(*(volatile unsigned char *)(0xFF210220))
#define BT_DivisorLatchMSB 					(*(volatile unsigned char *)(0xFF210222))

// addresses for the gaussian test
#define gaussian_accel_base (volatile int *) 	0xFF202040
#define component_factor 1000000.0

// image sizes
#define WIDTH	1920
#define HEIGHT	1080
#define DEPTH	3

/**************************************************************************
** Subroutine to initialise the BlueTooth Port by writing some data
** to the internal registers.
** Call this function at the start of the program before you attempt
** to read or write to data via the RS232 port
**
** Refer to UART data sheet for details of registers and programming
***************************************************************************/

// function signatures
void Init_BT(void);
int putcharBT(int c);
int getcharBT( void );
int BTTestForReceivedData(void);
void BTFlush(void);
void apply_gaussian(unsigned char[HEIGHT][WIDTH][DEPTH], unsigned char[HEIGHT][WIDTH][DEPTH]);

void Init_BT(void)
{
	printf("Reached Init_BT function\n");

	// set bit 7 of Line Control Register to 1, to gain access to the baud rate registers
	BT_LineControlReg = BT_LineControlReg | 0x80;

	// set Divisor latch (LSB and MSB) with correct value for required baud rate
	int baudRate = 9600;
	int divisorVal = 50000000 / baudRate / 16;
	// set least significant bits
	BT_DivisorLatchLSB = divisorVal & 0xff;
	// set most significant bits
	BT_DivisorLatchMSB = (divisorVal >> 8) & 0xff;

	// set bit 7 of Line control register back to 0 and
	// program other bits in that reg for 8 bit data, 1 stop bit, no parity etc
	// bit 1 and 0 are set to 11  for 8 bit data
	// bit 2 is set to 0 for 1 stop bit
	// bit 3 is set to 0 for no parity
	// bit 4 is not used since there is no parity (set to 0 here)
	// bit 5 is ignored (set to 0 here)
	// bit 6 is set to 0 for normal operation
	// bit 7 is set to 0 for fifo access
	// 00000011 = 0x03
	BT_LineControlReg = 0x03;

	// Reset the Fifo's in the FiFo Control Reg by setting bits 1 & 2
	BT_FifoControlReg = BT_FifoControlReg | 0x06;

	// Now Clear all bits in the FiFo control registers
	BT_FifoControlReg = 0x00;

}

int putcharBT(int c)
{
	// wait for Transmitter Holding Register bit (5) of line status register to be '1'
	// indicating we can write to the device
	while( (BT_LineStatusReg & 0x20) != 0x20 ){
		// wait
	}

	// write character to Transmitter fifo register
	BT_TransmitterFifo = (char) c;

	// return the character we printed
	return c;

}

int getcharBT( void )
{
	// wait for Data Ready bit (0) of line status register to be '1'
	while ((BT_LineStatusReg & 0x01) != 0x01)
	{
		// wait
	}
	// read new character from ReceiverFiFo register
	char data = BT_ReceiverFifo;

	// return new character
	return (int) data;

}

// the following function polls the UART to determine if any character
// has been received. It doesn't wait for one, or read it, it simply tests
// to see if one is available to read from the FIFO
int BTTestForReceivedData(void)
{
	// if BT_LineStatusReg bit 0 is set to 1
	if( (BT_LineStatusReg & 0x01) == 0x01 ){
		return 1;
	}
	//return TRUE, otherwise return FALSE
	return 0;
}

//
// Remove/flush the UART receiver buffer by removing any unread characters
//
void BTFlush(void)
{
	char data = '\0';
	// while bit 0 of Line Status Register == �1�
	// read unwanted char out of fifo receiver buffer
	while ((BT_LineStatusReg & 0x01) == 0x01){
		data = BT_ReceiverFifo;
	}
	// return; // no more characters so return
	return;
}

void apply_gaussian(unsigned char input[HEIGHT][WIDTH][DEPTH], unsigned char output[HEIGHT][WIDTH][DEPTH]) {
    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int d = 0; d < DEPTH; d++) {
                int top = (i == 0) ? 0 : i - 1;
                int bottom = (i == HEIGHT - 1) ? (HEIGHT - 1) : (i + 1);
                int left = (j == 0) ? 0 : j - 1;
                int right = (j == WIDTH - 1) ? (WIDTH - 1) : (j + 1);
                *(gaussian_accel_base + 1) = input[top][left][d];
                *(gaussian_accel_base + 2) = input[top][j][d];
                *(gaussian_accel_base + 3) = input[top][right][d];
                *(gaussian_accel_base + 4) = input[i][left][d];
                *(gaussian_accel_base + 5) = input[i][j][d];
                *(gaussian_accel_base + 6) = input[i][right][d];
                *(gaussian_accel_base + 7) = input[bottom][left][d];
                *(gaussian_accel_base + 8) = input[bottom][j][d];
                *(gaussian_accel_base + 9) = input[bottom][right][d];


                // *(gaussian_accel_base) will return output value scaled up by 10^6
                output[i][j][d] = (int) round(*(gaussian_accel_base) / component_factor);
            }
        }
    }
}

int main(void){
	const int SIZE = 256;
	unsigned char data[SIZE];


	Init_BT();

	printf("Finished BlueTooth initialization\n");

	// get data that is being sent
	for(int i = 0; i < SIZE; i++){
		data[i] = getcharBT();
		printf("received the %d byte as %d\n", i, data[i]);
	}

	printf("DE1 received all data\n");

	// send data back
	for(int i = 0; i < SIZE; i++){
		putcharBT(data[i]);
	}

	printf("DE1 sent all data back\n");

	return 0;
}
