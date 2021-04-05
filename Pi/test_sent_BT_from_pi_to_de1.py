import serial
import numpy as np

try:
    # this variable controls the amount of data sent
    size = 256

    # initialize serial port
    ser = serial.Serial('/dev/rfcomm0', 9600)
    if ser.isOpen == False:
        ser.open()

    # create data to send
    data = np.arange(size, dtype=np.uint8)

    # send data
    for i in range(size):
        byte = bytes([int(data[i])])
        ser.write(byte)
        print("Sent data:", byte)

    # wait for data
    while(ser.in_waiting == 0):
        pass

    # receive data
    received_data = np.empty(size,dtype=np.uint8)
    for i in range(size):
        received_data[i] = ord(ser.read())

    print("received", received_data)
    if((data == received_data).all()):
        print("sent and received data is the same")
    else:
        print("sent and received data are not the same")


except KeyboardInterrupt as e:
    print(e)
finally:
    ser.close()