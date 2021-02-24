from flask import Flask, render_template, request
import Retrieve as ret
Flask_App = Flask(__name__)  # Creating our Flask Instance


@Flask_App.route('/', methods=['GET'])
def index():
    """ Displays the index page accessible at '/' """

    return render_template('index.html')


@Flask_App.route('/operation_result/', methods=['POST'])
def operation_result():


    error = None
    result = None
    first_input = request.form['Input1']
    second_input = request.form['Input2']

    try:
        input1 = str(first_input)
        input2 = int(second_input)
        result = ret.loadRetrieverObjects(input1,input2)

        return render_template(
            'index.html',
            input1=input1,
            input2=input2,
            result=result,
            calculation_success=True
        )

    except:
        pass


if __name__=='__main__':
    Flask_App.debug = True
    Flask_App.run()
