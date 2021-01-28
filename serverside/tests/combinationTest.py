import sys
import os
sys.path.append(os.getcwd())
from modules.combination import comb
import unittest

class Test(unittest.TestCase):
    def testComb1(self):
        print("test combination 1 start")
        self.assertEqual(comb(3,2), 3)
        print("test combination 1 end\n")
    
    def testComb2(self):
        print("test combination 2 start")
        self.assertGreaterEqual(comb(8,4), 8)
        print("test combination 2 end")

if __name__ == "__main__":
    unittest.main()