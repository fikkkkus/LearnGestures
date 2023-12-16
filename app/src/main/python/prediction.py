import numpy as np
import cv2
import tensorflow as tf
from os.path import dirname, join

def resize_and_grayscale(image_path, size=(28, 28)):
	img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
	print(img.shape)
	img_resized = cv2.resize(img, size)
	img_matrix = np.array(img_resized) / 255
	img_matrix = img_matrix.reshape(-1, 28, 28, 1)
	return img_matrix


def predictGesture(image_path):
	path = join(dirname(__file__), "model/model.tflite")

	interpreter = tf.lite.Interpreter(model_path=path)
	interpreter.allocate_tensors()

	input_details = interpreter.get_input_details()
	output_details = interpreter.get_output_details()

	img_matrix = resize_and_grayscale(image_path)

	img_matrix = img_matrix.astype(np.float32)

	img_matrix = img_matrix.reshape(input_details[0]['shape'])

	interpreter.set_tensor(input_details[0]['index'], img_matrix)

	interpreter.invoke()

	output_data = interpreter.get_tensor(output_details[0]['index'])
	predicted_class = np.argmax(output_data)

	letters = "ABCDEFGHIKLMNOPQRSTUVWXY"
	classes = dict([(class_num, letter) for class_num, letter in enumerate(list(letters))])

	print(predicted_class, classes[predicted_class])

	return classes[predicted_class]