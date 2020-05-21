from flask import Flask, render_template, url_for, request, redirect
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///../cinema.db'
db = SQLAlchemy(app)


class Movies(db.Model):
    id_m = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    description = db.Column(db.String(200), nullable=False)
    image_url = db.Column(db.String(200), nullable=False)

    def __repr__(self):
        return '<Movie %r>' % self


@app.route('/', methods=['POST', 'GET'])
def index():
    if request.method == 'POST':
        movie_name = request.form['name']
        movie_description = request.form['description']
        movie_image_url = request.form['image_url']
        new_movie = Movies(name=movie_name, description=movie_description, image_url=movie_image_url)

        try:
            db.session.add(new_movie)
            db.session.commit()
            return redirect('/')
        except:
            return 'There was an issue adding your movie'

    else:
        movies = Movies.query.order_by(Movies.name).all()
        return render_template('index.html', movies=movies)


@app.route('/delete/<int:id_m>')
def delete(id_m):

    movie_to_delete = Movies.query.get_or_404(id_m)

    try:
        db.session.delete(movie_to_delete)
        db.session.commit()
        return redirect('/')

    except:
        return 'There was a problem deleting the movie'


@app.route('/update/<int:id_m>', methods=['GET', 'POST'])
def update(id_m):
    movie = Movies.query.get_or_404(id_m)

    if request.method == 'POST':
        movie.name = request.form['name']
        movie.description = request.form['description']
        movie.image_url = request.form['image_url']

        try:
            db.session.commit()
            return redirect('/')
        except:
            return 'There was an issue updating your movie'

    else:
        return render_template('update.html', movie=movie)


if __name__ == "__main__":
    app.run(debug=True)
