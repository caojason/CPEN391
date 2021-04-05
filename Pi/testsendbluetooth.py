import socket

serverMACAddress = 'C8:34:8E:09:66:FC'
clientMACAddress = 'DC:A6:32:30:25:99' 
channel = 5

s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
# s.bind((clientMACAddress,channel))
s.connect((serverMACAddress,channel))

print("successfully connected")

while 1:
    text = input("Enter your input: ")
    if text == "quit":
        break
    s.send(text.encode())
s.close()
