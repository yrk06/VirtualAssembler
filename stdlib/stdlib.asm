; The address for the first instruction must be at position 128
.space 128
.fill _start
; ================================= ;

; Display string of text into stdout
; x1 - Callee address 
; x2 - Address of string
; x3 - String length
_print:	add x3, x3, x2
		mov x5, 1024
_print_loop:	
		lw x4, x2, 0
		sw x4, x5, 0
		addi x2, x2, 1
		beq x2, x3, _print_exit
		beq x0,x0, _print_loop
_print_exit:
		ret

_println:
		addi sp, sp, -1
		sw x1,sp,0
		call _print	
		lw x1,sp,0
		addi sp,sp,1
_print_newline:
		mov x2, 10
		mov x3, 1024
		sw x2,x3,0
		ret

; Prints a number into stdout
; x1 - Callee address
; x2 - Number to print
_printNumber:
	addi sp,sp,-1
	mov x3, 0
	sw x3,sp,0
_printNumber_loop:
	addi sp,sp,-1
	sw x1,sp,0
	mov x3, 10
	call _divide
	lw x1,sp,0
	addi sp,sp,1
	; x2 tem o resultado
	; x3 tem o resto
	; Carregar a quantidade de números
	lw x5, sp, 0
	; Incrementar a quantidade de digitos
	addi x5,x5,1
	addi sp,sp,-1
	sw x5,sp,0
	; Salvar o resto
	sw x3,sp,1
	mov x3, 0
	beq x2,x3, _printNumber_print
	beq sp,sp,_printNumber_loop

_printNumber_print:
	mov x3, 0
	mov x4, 1024
	lw x5,sp,0
	addi sp,sp,1
_printNumber_print_loop:
	lw x2,sp,0
	addi x2,x2,48
	sw x2,x4,0
	addi x5,x5,-1
	addi sp,sp,1
	beq x5,x3, _printNumber_end
	beq x5,x5,_printNumber_print_loop
_printNumber_end:
	ret
; =============================== ;

; Compares 2 numbers
; x1 - address for branching if greater
; x2 - address for branching if smaller
; x3 - number to compare
; x4 - number to compare
_cmp:	nand x4,x4,x4
		addi x4,x4,1
		add x3,x3,x4
		mov x5,-32768
		nand x3,x3,x5
		mov x5, 32767
		beq x3,x5,_cpm_sm
_cpm_ge:
		jalr x1,x1
_cpm_sm:
		jalr x2,x2

; =============================== ;

; Subtract two numbers
; x1 - Callee address
; x2 - Base number
; x3 - Number to subtract
_subtract:	
		nand x3, x3, x3
		addi x3, x3, 1
		add x2, x2, x3
		ret

; ============================= ;

; Multiplies two numbers
; x1 - Callee address
; x2 - base number
; x3 - multiplier
_multiply:
		mov x4,0
		beq x3,x4,_multiply_zero
		mov x5, 0
_multiply_loop:	
		add x5,x5,x2
		addi x3,x3,-1
		beq x3,x4,_multiply_end
		beq x3,x3,_multiply_loop

_multiply_end:
		mov x2, 0
		add x2,x2,x5
		ret

_multiply_zero:
		mov x2, 0
		beq x1,x1,_multiply_end	

; ============================= ;

; Divides 2 numbers and return the result and remainder
; x1 - Callee Address
; x2 - Number to be divided
; x3 - Divisor
_divide:
		mov x5,0
		addi sp,sp,-4
		sw x1, sp, 0
		sw x2, sp, 1
		sw x3, sp, 2
		sw x5, sp, 3
_divide_loop:
		lw x3, sp, 1
		lw x4, sp, 2
		mov x1, _cmp
		mov x2, _divide_is_smaller
		jalr x1,x1
_divide_is_bigger:
		lw x2, sp, 1
		lw x3, sp, 2
		; subtrair
		nand x3,x3,x3
		addi x3,x3,1
		add x2,x2,x3
		; Incrementar o resultado
		lw x5, sp,3
		addi x5,x5,1
		sw x5, sp,3
		; Salvar o que sobrou
		sw x2, sp, 1
		beq sp,sp,_divide_loop
_divide_is_smaller:
		lw x2, sp, 3
		lw x3, sp, 1
		lw x1, sp, 0
		addi sp, sp, 4
		ret

; ============================= ;
		
_exit:
		halt
