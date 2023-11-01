import os
from PIL import Image

def count_images_in_folder(folder_path):
    image_count = 0

    # Iterate over the files in the folder
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)
        
        # Check if the file is an image
        try:
            with Image.open(file_path) as img:
                image_count += 1
        except (IOError, OSError, Image.DecompressionBombError):
            pass  # Not an image

    return image_count

def check_subfolders_for_images(main_folder):
    subfolder_results = []

    for root, _, _ in os.walk(main_folder):
        image_count = count_images_in_folder(root)
        subfolder_name = os.path.basename(root)  # Get the folder name without the full path

        # Exclude the main folder from the count
        if root != main_folder:
            subfolder_results.append((subfolder_name, image_count))

    return subfolder_results

if __name__ == "__main__":
    main_folder = r"C:\Users\mzowe\Documents\SSSDataSets\ImageCNN\SplittedProductsDataSets\validation"  # Replace with the path to your specific folder
    results = check_subfolders_for_images(main_folder)

    empty_subfolders = [folder for folder, image_count in results if image_count == 0]

    for folder, image_count in results:
        if image_count == 0:
            print(f"Subfolder {folder} contains no images.")
        else:
            print(f"Subfolder {folder} contains {image_count} images.")

    # Number of subfolders (excluding the main one)
    num_subfolders = len(results)

    # Summary
    total_images = sum(image_count for _, image_count in results)
    
    if total_images > 0:
        print(f"Total number of images in all subfolders: {total_images}")
    else:
        print("No images found in any subfolder.")
    
    if empty_subfolders:
        print("Subfolders with no images:")
        for folder in empty_subfolders:
            print(folder)
    
    # Print the number of subfolders
    print(f"Number of subfolders (excluding the main one): {num_subfolders}")
