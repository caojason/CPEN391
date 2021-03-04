import base64

def encode_img():
    with open("3x3.png", "rb+") as f:
        encoded_img = base64.b64encode(f.read())
    print(encoded_img.decode("utf-8"))

encode_img()