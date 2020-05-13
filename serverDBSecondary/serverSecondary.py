import socket
from threading import Thread, Lock
from socketserver import ThreadingMixIn

import sqlite3
from time import sleep
from datetime import datetime


class Showtime:

    def __init__(self, showtime_datetime, seats_available):
        self.showtime_datetime = showtime_datetime
        self.seats_available = seats_available

    def get_message(self, msg_id):
        msg = msg_id.to_bytes(4, 'big') + \
              bytes(self.encode_datetime(), 'utf-8') + \
              self.seats_available.to_bytes(4, 'big')

        msg = len(msg).to_bytes(4, 'big') + msg

        return msg

    def encode_datetime(self):
        x = datetime.strptime(self.showtime_datetime, "%Y-%m-%d %H:%M:%S")
        return x.strftime("%d.%m - %H:%M")

    @staticmethod
    def decode_datetime(encoded_datetime):
        x = datetime.strptime(encoded_datetime, "%d.%m - %H:%M")
        return x


# Multi-threaded Python server : TCP Server Socket Thread Pool
class ClientThread(Thread):

    def __init__(self, ip, port, client_socket):
        Thread.__init__(self)
        self.ip = ip
        self.port = port
        self.client_socket = client_socket
        print("[+] New server socket thread started for " + ip + ":" + str(port))

    def run(self):
        msg_len, msg_id = self.get_msg_id()
        if msg_id == 4:
            self.handle_showtime_request()
        elif msg_id == 7:
            self.handle_booking_request(msg_len)

    def get_msg_id(self):
        data = self.client_socket.recv(8, socket.MSG_WAITALL)
        msg_len = int.from_bytes(data[0:4], byteorder='big')
        msg_id = int.from_bytes(data[4:8], byteorder='big')

        if msg_id < 3:  # TODO: add msg_id boundaries
            print("Invalid msg_id(")
            exit(1)

        return msg_len, msg_id

    def handle_showtime_request(self):
        data = self.client_socket.recv(4, socket.MSG_WAITALL)
        movie_id = int.from_bytes(data[0:4], byteorder='big')

        msg_id = 5

        db = sqlite3.connect('cinema.db')
        db_cursor = db.cursor()
        db_cursor.execute("SELECT * FROM showtimes WHERE id_m = " + str(movie_id))
        rows = db_cursor.fetchall()

        showtimes = []
        for row in rows:
            showtimes.append(Showtime(row[2], row[3]))

        for showtime in showtimes:
            self.client_socket.sendall(showtime.get_message(msg_id))

        msg_id = 6

        end_msg = msg_id.to_bytes(4, 'big')
        end_msg = len(end_msg).to_bytes(4, 'big') + end_msg
        self.client_socket.sendall(end_msg)

        print("Successfully sent showtime list")

        db_cursor.close()
        db.close()

    def handle_booking_request(self):
        # TODO: finish this
        data = self.client_socket.recv(4, socket.MSG_WAITALL)
        seats = int.from_bytes(data[0:4], byteorder='big')

        data = self.client_socket.recv(13, socket.MSG_WAITALL)
        # showtime =

        def decode_datetime(encoded_datetime):
            x = datetime.strptime(encoded_datetime, "%d.%m - %H:%M")
            return x

        msg_id = 8

        db = sqlite3.connect('cinema.db')
        db_cursor = db.cursor()
        db_cursor.execute("SELECT * FROM showtimes WHERE  = " + str())
        rows = db_cursor.fetchall()



# Multi-threaded Python server : TCP Server Socket Program Stub
HOST_IP = 'localhost'
PORT = 2005
BUFFER_SIZE = 20  # Usually 1024, but we need quick response

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST_IP, PORT))
threads = []

server.listen(4)
print("Listening...")

while True:
    (client_socket, (ip, port)) = server.accept()

    new_thread = ClientThread(ip, port, client_socket)
    new_thread.start()
    threads.append(new_thread)

# for t in threads:
#     t.join()
