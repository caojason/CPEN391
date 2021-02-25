import os
import sys
sys.path.append(os.getcwd())
import modules.database.store_info_database as UD

from app import app

from flask import Flask, request, jsonify

