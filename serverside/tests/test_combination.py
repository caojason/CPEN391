import sys
import os
sys.path.append(os.getcwd())
from modules.combination import comb

def testComb1():
    assert comb(3,2) == 3
    
def testComb2():
    assert comb(8,4) != 8

