import math

def comb(a, b):
    upper = math.factorial(a)
    lower = math.factorial(b) * math.factorial(a - b)
    return upper / lower