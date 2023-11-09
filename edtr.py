from PIL import Image
import os
import shutil

def rotate_and_copy_images(folder_path):
    # Ensure the folder path exists
    if not os.path.exists(folder_path):
        print(f"Folder '{folder_path}' does not exist.")
        return

    image_files = [f for f in os.listdir(folder_path) if f.lower().endswith(('.png', '.jpg', '.jpeg', '.gif'))]

    if len(image_files) != 10:
        print(f"The folder '{folder_path}' must contain 10 image files.")
        return

    # Create a directory for the output images
    output_folder = os.path.join(folder_path, 'output')
    os.makedirs(output_folder, exist_ok=True)

    for image_file in image_files:
        image_path = os.path.join(folder_path, image_file)
        original_image = Image.open(image_path)

        # Rotate and copy the original image
        for i in range(4):
            rotated_image = original_image.rotate(90 * i)
            rotated_image.save(os.path.join(output_folder, f'{i * 90}_{image_file}'))

    # Copy all rotated images back to the output folder (to have a total of 80 images)
    for rotated_image_file in os.listdir(output_folder):
        rotated_image_path = os.path.join(output_folder, rotated_image_file)
        rotated_image = Image.open(rotated_image_path)
        rotated_image.save(os.path.join(output_folder, f'copy_{rotated_image_file}'))

    print(f"Processed {len(image_files)} images in '{folder_path}'.")

    # Delete the original 10 images in the main folder
    for image_file in image_files:
        os.remove(os.path.join(folder_path, image_file))
        print(f"Deleted '{image_file}' in '{folder_path}'.")

    # Move all rotated images back to the main folder
    for rotated_image_file in os.listdir(output_folder):
        moved_image_path = os.path.join(folder_path, rotated_image_file)
        shutil.move(os.path.join(output_folder, rotated_image_file), moved_image_path)

    # Remove the empty "output" folder
    os.rmdir(output_folder)
    print(f"Removed the 'output' folder in '{folder_path}'.")

if __name__ == "__main__":
    folder_paths = [
        r"C:\Users\mzowe\Documents\SSSDataSets\ImageCNN\ProductsDataSets\Olivenza Matches",
        r"C:\Users\mzowe\Documents\SSSDataSets\ImageCNN\ProductsDataSets\StarMargarin Sweetblend",
        # Add more folder paths as needed
    ]

    for folder_path in folder_paths:
        rotate_and_copy_images(folder_path)
