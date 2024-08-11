from flask import Flask, render_template, request, jsonify, send_file, send_from_directory
from flask_socketio import SocketIO, emit
import subprocess
import webbrowser
import os
import select
import json
import re

app = Flask(__name__)
socketio = SocketIO(app)
process = None


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/help')
def help():
    return render_template('help.html')


@app.route('/test_report.html')
def get_test_report():
    return send_file('work-dir/test_report.html')


@app.route('/empty.html')
def get_empty():
    return send_file('templates/empty.html')


@app.route('/progress.html')
def get_progress():
    return send_file('templates/progress.html')


@app.route('/icons/<path:filename>')
def get_icon(filename):
    return send_from_directory('static/icon', filename)


@socketio.on('run_java')
def handle_run_java(json):
    """
    pro spusteni konfigurace primo na java nastroji. nepterzite ukazuje vystup logovani a po jejim dokonceni je mozne zobrazit vystupni report
    """
    global process
    if process and process.poll() is None:
        emit('output', {'data': 'Process is already running.'})
        return

    yaml_content = json.get('yaml_content')
    if not yaml_content:
        emit('finished', {'status': 'error',
             'message': 'Missing YAML content!'})
        return

    with open('tmp-config.yaml', 'w') as yaml_file:
        yaml_file.write(yaml_content)

    try:
        process = subprocess.Popen(['java', '-jar', '../NATT.jar', '-c', '../tmp-config.yaml'],
                                   stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, cwd='./work-dir')
        emit('output', {
             'data': '<span class="green-text">NATT testing tool is running now</span>'})

        streams = [process.stdout, process.stderr]
        while True:
            ready_streams, _, _ = select.select(streams, [], [])
            for stream in ready_streams:
                output = stream.readline()
                if output:
                    if stream is process.stdout:
                        emit('output', {'data': output.rstrip()})
                    else:
                        emit('output', {
                             'data': '<span class="red-text">' + output.rstrip() + '</span>'})

            if process.poll() is not None:
                break

        exit_code = process.wait()
        if exit_code == 0:
            emit('finished', {'status': 'success'})
        else:
            emit('finished', {
                 'status': 'error', 'message': f'Program ends with status code: {exit_code}'})
    except Exception as e:
        if process is not None:
            emit('finished', {'status': 'error', 'message': str(e)})
    finally:
        if process:
            process.stdout.close()
            process.stderr.close()
            process.terminate()


@socketio.on('stop_java')
def handle_stop_java():
    """
    zastaveni spustene aplikace
    """
    global process
    if process:
        process.terminate()  # posle signal SIGTERM
        process = None
        emit('stopped', {
             'message': 'The testing process has been terminated by editor ...'})


@socketio.on('validate')
def handle_validation(json):
    yaml_content = json.get('yaml_content')
    if not yaml_content:
        emit('validate-response', {'status': 'error',
             'message': 'Missing YAML content!'})
        return

    with open('tmp-config.yaml', 'w') as yaml_file:
        yaml_file.write(yaml_content)

    try:
        val_process = subprocess.Popen(['java', '-jar', '../NATT.jar', '-c', '../tmp-config.yaml', '-v'],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, cwd='./work-dir')
        stdout, stderr = val_process.communicate()

        if val_process.returncode == 0:
            emit('validate-response', {'status': 'success',
                                       'message': 'Configuration is valid'})
        else:
            emit('validate-response', {'status': 'error', 'message': stderr})
    except Exception as e:
        emit('validate-response', {'status': 'error', 'message': str(e)})
    finally:
        if process:
            process.stdout.close()
            process.stderr.close()
            process.terminate()

@app.route('/get-snippets', methods=['POST'])
def get_snippets():
    try:
        # Define the path to the NATT.jar and working directory
        jar_path = '../NATT.jar'
        work_dir = './work-dir'

        # Execute the command to run NATT.jar and capture the output
        result = subprocess.run(
            ['java', '-jar', jar_path, '-kd'],
            cwd=work_dir,
            text=True,
            capture_output=True
        )

        # Check if the command was successful
        if result.returncode != 0:
            return jsonify({"error": f"Error executing command: {result.stderr}"}), 500

        # Extract the keyword list from the command output
        keyword_list_match = re.search(
            r'Documentation for registered keywords:\s*\[(.*)\]', result.stdout, re.S)
        if not keyword_list_match:
            return jsonify({"error": "Failed to find the keyword list in the output."}), 500

        keyword_list_string = f'[{keyword_list_match.group(1)}]'

        # Parse the keyword list JSON
        try:
            keyword_list = json.loads(keyword_list_string)
        except json.JSONDecodeError as parse_error:
            return jsonify({"error": f"Error parsing JSON: {str(parse_error)}"}), 500

        # Create the keyword snippets
        keyword_snippets = []
        for keyword in keyword_list:
            snippet = {
                "caption": keyword["name"],
                "snippet": f'{keyword["name"]}:\n' + '\n'.join([
                    f'    {param}: ' + {
                        "STRING": '"example"',
                        "LONG": "100",
                        "DOUBLE": "10.5",
                        "BOOLEAN": "true",
                        "LIST": "[]"
                    }.get(keyword["types"][i], "example value")
                    for i, param in enumerate(keyword["parameters"])
                ]),
                "meta": f'{keyword["kwGroup"]} - {keyword["description"]}'
            }
            keyword_snippets.append(snippet)

        # Return the generated snippets as JSON
        return jsonify({"snippets": keyword_snippets})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


def showPage():
    webbrowser.open_new("http://127.0.0.1:5000")


if __name__ == '__main__':
    if not os.environ.get("WERKZEUG_RUN_MAIN"):
        webbrowser.open_new('http://127.0.0.1:2000/')
    app.run(host="127.0.0.1", port=2000)
