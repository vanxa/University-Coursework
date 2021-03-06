; Generated by PSoC Designer 5.0.1127.0
;
;
;  PSoCGPIOINT.inc
;
;  Data: 29 October, 2001
;  Copyright (c) Cypress Semiconductor 2009. All Rights Reserved.
;
;  This file is generated by the Device Editor on Application Generation.
;  It contains equates that are useful in writing code relating to GPIO
;  related interrupts.
;  
;  DO NOT EDIT THIS FILE MANUALLY, AS IT IS OVERWRITTEN!!!
;  Edits to this file will not be preserved.
;
; FIFOP address and mask equates
FIFOP_Data_ADDR:	equ	ch
FIFOP_DriveMode_0_ADDR:	equ	10ch
FIFOP_DriveMode_1_ADDR:	equ	10dh
FIFOP_DriveMode_2_ADDR:	equ	fh
FIFOP_GlobalSelect_ADDR:	equ	eh
FIFOP_IntCtrl_0_ADDR:	equ	10eh
FIFOP_IntCtrl_1_ADDR:	equ	10fh
FIFOP_IntEn_ADDR:	equ	dh
FIFOP_MASK:	equ	80h
; SWITCH address and mask equates
SWITCH_Data_ADDR:	equ	14h
SWITCH_DriveMode_0_ADDR:	equ	114h
SWITCH_DriveMode_1_ADDR:	equ	115h
SWITCH_DriveMode_2_ADDR:	equ	17h
SWITCH_GlobalSelect_ADDR:	equ	16h
SWITCH_IntCtrl_0_ADDR:	equ	116h
SWITCH_IntCtrl_1_ADDR:	equ	117h
SWITCH_IntEn_ADDR:	equ	15h
SWITCH_MASK:	equ	8h
; ResetN address and mask equates
ResetN_Data_ADDR:	equ	4h
ResetN_DriveMode_0_ADDR:	equ	104h
ResetN_DriveMode_1_ADDR:	equ	105h
ResetN_DriveMode_2_ADDR:	equ	7h
ResetN_GlobalSelect_ADDR:	equ	6h
ResetN_IntCtrl_0_ADDR:	equ	106h
ResetN_IntCtrl_1_ADDR:	equ	107h
ResetN_IntEn_ADDR:	equ	5h
ResetN_MASK:	equ	8h
; SCLK address and mask equates
SCLK_Data_ADDR:	equ	ch
SCLK_DriveMode_0_ADDR:	equ	10ch
SCLK_DriveMode_1_ADDR:	equ	10dh
SCLK_DriveMode_2_ADDR:	equ	fh
SCLK_GlobalSelect_ADDR:	equ	eh
SCLK_IntCtrl_0_ADDR:	equ	10eh
SCLK_IntCtrl_1_ADDR:	equ	10fh
SCLK_IntEn_ADDR:	equ	dh
SCLK_MASK:	equ	1h
; FIFO address and mask equates
FIFO_Data_ADDR:	equ	ch
FIFO_DriveMode_0_ADDR:	equ	10ch
FIFO_DriveMode_1_ADDR:	equ	10dh
FIFO_DriveMode_2_ADDR:	equ	fh
FIFO_GlobalSelect_ADDR:	equ	eh
FIFO_IntCtrl_0_ADDR:	equ	10eh
FIFO_IntCtrl_1_ADDR:	equ	10fh
FIFO_IntEn_ADDR:	equ	dh
FIFO_MASK:	equ	2h
; MOSI address and mask equates
MOSI_Data_ADDR:	equ	ch
MOSI_DriveMode_0_ADDR:	equ	10ch
MOSI_DriveMode_1_ADDR:	equ	10dh
MOSI_DriveMode_2_ADDR:	equ	fh
MOSI_GlobalSelect_ADDR:	equ	eh
MOSI_IntCtrl_0_ADDR:	equ	10eh
MOSI_IntCtrl_1_ADDR:	equ	10fh
MOSI_IntEn_ADDR:	equ	dh
MOSI_MASK:	equ	4h
; SFD address and mask equates
SFD_Data_ADDR:	equ	ch
SFD_DriveMode_0_ADDR:	equ	10ch
SFD_DriveMode_1_ADDR:	equ	10dh
SFD_DriveMode_2_ADDR:	equ	fh
SFD_GlobalSelect_ADDR:	equ	eh
SFD_IntCtrl_0_ADDR:	equ	10eh
SFD_IntCtrl_1_ADDR:	equ	10fh
SFD_IntEn_ADDR:	equ	dh
SFD_MASK:	equ	8h
; MISO address and mask equates
MISO_Data_ADDR:	equ	ch
MISO_DriveMode_0_ADDR:	equ	10ch
MISO_DriveMode_1_ADDR:	equ	10dh
MISO_DriveMode_2_ADDR:	equ	fh
MISO_GlobalSelect_ADDR:	equ	eh
MISO_IntCtrl_0_ADDR:	equ	10eh
MISO_IntCtrl_1_ADDR:	equ	10fh
MISO_IntEn_ADDR:	equ	dh
MISO_MASK:	equ	10h
; CCA address and mask equates
CCA_Data_ADDR:	equ	ch
CCA_DriveMode_0_ADDR:	equ	10ch
CCA_DriveMode_1_ADDR:	equ	10dh
CCA_DriveMode_2_ADDR:	equ	fh
CCA_GlobalSelect_ADDR:	equ	eh
CCA_IntCtrl_0_ADDR:	equ	10eh
CCA_IntCtrl_1_ADDR:	equ	10fh
CCA_IntEn_ADDR:	equ	dh
CCA_MASK:	equ	20h
; CSn address and mask equates
CSn_Data_ADDR:	equ	10h
CSn_DriveMode_0_ADDR:	equ	110h
CSn_DriveMode_1_ADDR:	equ	111h
CSn_DriveMode_2_ADDR:	equ	13h
CSn_GlobalSelect_ADDR:	equ	12h
CSn_IntCtrl_0_ADDR:	equ	112h
CSn_IntCtrl_1_ADDR:	equ	113h
CSn_IntEn_ADDR:	equ	11h
CSn_MASK:	equ	2h
; LED_RED address and mask equates
LED_RED_Data_ADDR:	equ	14h
LED_RED_DriveMode_0_ADDR:	equ	114h
LED_RED_DriveMode_1_ADDR:	equ	115h
LED_RED_DriveMode_2_ADDR:	equ	17h
LED_RED_GlobalSelect_ADDR:	equ	16h
LED_RED_IntCtrl_0_ADDR:	equ	116h
LED_RED_IntCtrl_1_ADDR:	equ	117h
LED_RED_IntEn_ADDR:	equ	15h
LED_RED_MASK:	equ	1h
; LED_BLUE address and mask equates
LED_BLUE_Data_ADDR:	equ	14h
LED_BLUE_DriveMode_0_ADDR:	equ	114h
LED_BLUE_DriveMode_1_ADDR:	equ	115h
LED_BLUE_DriveMode_2_ADDR:	equ	17h
LED_BLUE_GlobalSelect_ADDR:	equ	16h
LED_BLUE_IntCtrl_0_ADDR:	equ	116h
LED_BLUE_IntCtrl_1_ADDR:	equ	117h
LED_BLUE_IntEn_ADDR:	equ	15h
LED_BLUE_MASK:	equ	2h
; LED_GREEN address and mask equates
LED_GREEN_Data_ADDR:	equ	14h
LED_GREEN_DriveMode_0_ADDR:	equ	114h
LED_GREEN_DriveMode_1_ADDR:	equ	115h
LED_GREEN_DriveMode_2_ADDR:	equ	17h
LED_GREEN_GlobalSelect_ADDR:	equ	16h
LED_GREEN_IntCtrl_0_ADDR:	equ	116h
LED_GREEN_IntCtrl_1_ADDR:	equ	117h
LED_GREEN_IntEn_ADDR:	equ	15h
LED_GREEN_MASK:	equ	4h
