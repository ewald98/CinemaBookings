import socket
from threading import Thread, Lock
from socketserver import ThreadingMixIn

import sqlite3
from time import sleep
from datetime import datetime

db_lock = Lock()


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
        return x.strftime("%m-%d %H:%M")


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

        self.client_socket.close()

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
        db_cursor.execute("SELECT * FROM showtimes WHERE `datetime` > datetime('now') AND  id_m = " + str(movie_id))
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

    def handle_booking_request(self, msg_len):
        # TODO: finish this
        data = self.client_socket.recv(12, socket.MSG_WAITALL)
        phone_no_len = int.from_bytes(data[0:4], byteorder='big')
        movie_id = int.from_bytes(data[4:8], byteorder='big')
        seats = int.from_bytes(data[8:12], byteorder='big')

        data = self.client_socket.recv(13, socket.MSG_WAITALL)
        encoded_showtime = data.decode("utf-8")
        decoded_partial_showtime = Showtime.decode_datetime(encoded_showtime)

        data = self.client_socket.recv(phone_no_len, socket.MSG_WAITALL)
        phone_no = data.decode("utf-8")

        name_len = msg_len - 29 - phone_no_len
        data = self.client_socket.recv(name_len, socket.MSG_WAITALL)
        name = data.decode("utf-8")

        db = sqlite3.connect('cinema.db')
        db_cursor = db.cursor()

        return_code = 0 # 0 stands for successful
        # get id_s using movie_id & showtime
        db_lock.acquire()
        db_cursor.execute(
            "SELECT id_s, seats_available FROM showtimes WHERE id_m = {0} AND instr(datetime, '{1}') > 0"
            .format(str(movie_id), decoded_partial_showtime)
        )
        rows = db_cursor.fetchall()
        if len(rows) == 0:
            print("error: showtime not found")
            return_code = 3
        elif len(rows) > 1:
            print("error: found more valid showtimes")
            return_code = 4
        elif len(rows) == 1:
            (showtime_id, seats_available) = rows[0]
            if seats > seats_available:
                print("error: not enough seats available!")
                return_code = 5

            if return_code == 0:
                db_cursor.execute(
                    "UPDATE showtimes SET seats_available = {0} WHERE id_s = {1}"
                    .format(seats_available - seats, showtime_id)
                )
                db.commit()

                db_cursor.execute(
                    "INSERT INTO bookings (id_s, seats, name, phone_no) VALUES (?, ?, ?, ?)",
                    (showtime_id, seats, name, phone_no)
                )
                db.commit()
        db_lock.release()

        msg_id = 8
        msg = msg_id.to_bytes(4, 'big') + \
              return_code.to_bytes(4, 'big')
        msg = len(msg).to_bytes(4, 'big') + msg

        self.client_socket.sendall(msg)

        print("Successfully sent confirmation")

        db_cursor.close()
        db.close()


# Multi-threaded Python server : TCP Server Socket Program
HOST_IP = 'localhost'
PORT = 2005

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

    for thread in threads:
        if not thread.is_alive():
            thread.join()
