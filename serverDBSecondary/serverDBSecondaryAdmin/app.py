from flask import Flask, render_template, url_for, request, redirect
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///../cinema.db'
db = SQLAlchemy(app)


class Showtimes(db.Model):
    id_s = db.Column(db.Integer, primary_key=True)
    id_m = db.Column(db.Integer, nullable=False)
    datetime = db.Column(db.Integer, nullable=False)
    seats_available = db.Column(db.String(200), nullable=False)
    movie_name = db.Column(db.String(200), nullable=False)

    def __repr__(self):
        return '<Showtime %r>' % self


@app.route('/', methods=['POST', 'GET'])
def index():
    if request.method == 'POST':
        showtime_id_m = request.form['id_m']
        showtime_movie_name = request.form['movie_name']
        showtime_datetime = request.form['datetime']
        showtime_seats_available = request.form['seats_available']
        new_showtime = Showtimes(id_m=showtime_id_m, datetime=showtime_datetime, seats_available=showtime_seats_available, movie_name=showtime_movie_name,)

        try:
            db.session.add(new_showtime)
            db.session.commit()
            return redirect('/')
        except:
            return 'There was an issue adding your movie'

    else:
        showtimes = Showtimes.query.order_by(Showtimes.datetime).all()
        return render_template('index.html', showtimes=showtimes)


@app.route('/delete/<int:id_s>')
def delete(id_s):

    showtime_to_delete = Showtimes.query.get_or_404(id_s)

    try:
        db.session.delete(showtime_to_delete)
        db.session.commit()
        return redirect('/')

    except:
        return 'There was a problem deleting the movie'


@app.route('/update/<int:id_s>', methods=['GET', 'POST'])
def update(id_s):
    showtime = Showtimes.query.get_or_404(id_s)

    if request.method == 'POST':
        showtime.datetime = request.form['datetime']
        showtime.seats_available = request.form['seats_available']

        try:
            db.session.commit()
            return redirect('/')
        except:
            return 'There was an issue updating your showtime'

    else:
        return render_template('update.html', showtime=showtime)


if __name__ == "__main__":
    app.run(debug=True)
