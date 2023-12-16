import numpy as np
import cv2
import tensorflow as tf
from os.path import dirname, join

def resize_and_grayscale(image_path, size=(28, 28)):
	img = cv2.imread(image_path)

	# Calculate the dimensions for cropping
	crop_size = min(img.shape[0], img.shape[1])
	crop_x = (img.shape[1] - crop_size) // 2
	crop_y = (img.shape[0] - crop_size) // 2

	# Crop the image
	cropped_img = img[crop_y:crop_y+crop_size, crop_x:crop_x+crop_size]

	# Resize the image to 28x28
	resized_img = cv2.resize(cropped_img, size, interpolation=cv2.INTER_AREA)

	# Convert the image to grayscale
	resized_img_gray = cv2.cvtColor(resized_img, cv2.COLOR_BGR2GRAY)

	# Normalize pixel values to the range [0, 1]
	img_matrix = np.array(resized_img_gray) / 255

	# Reshape the image for model input
	img_matrix = img_matrix.reshape(-1, size[0], size[1], 1)

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