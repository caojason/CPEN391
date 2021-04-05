import bluetooth

bd_addr = "c8:34:8e:09:66:fc"

port = 5

sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((bd_addr, port))

sock.send("hello!!")

sock.close()