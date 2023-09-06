function toggleDisplay(elementId) {
    var element = document.getElementById(elementId);
    if (element.style.display === 'none') {
        element.style.display = 'block';
    } else {
        element.style.display = 'none';
    }
}

function hideAllExcept(elementId) {
    const idsToHide = [
        "upload-content", 
        "request-signature-form", 
        "signature-status-content", 
        "file1-upload-content", 
        "pdf-check-content" // Add the ID of the new content div here
    ];
    
    idsToHide.forEach(function(id) {
        if (id !== elementId) {
            document.getElementById(id).style.display = 'none';
        }
    });

    toggleDisplay(elementId);
}

document.getElementById("upload-file-button").addEventListener("click", function() {
    hideAllExcept("upload-content");
});

document.getElementById("request-signature-button").addEventListener("click", function() {
    hideAllExcept("request-signature-form");
});

document.getElementById("summary-button").addEventListener("click", function() {
    hideAllExcept("signature-status-content");
});

document.getElementById("sign-file-button").addEventListener("click", function() {
    hideAllExcept("file1-upload-content");
});

// New code for your 'Check PDF' button
document.getElementById("check-pdf-button").addEventListener("click", function() {
    hideAllExcept("pdf-check-content");
});




var uploadArea = document.getElementById('upload-area');
var fileInput = document.getElementById('file-input');
var dragText = document.querySelector('.drag-text');

uploadArea.addEventListener('dragover', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
});

uploadArea.addEventListener('dragleave', function() {
    this.style.backgroundColor = 'transparent';
});

uploadArea.addEventListener('drop', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'transparent';
    fileInput.files = e.dataTransfer.files;
    updateFileNameDisplay();
});

document.querySelector('.browse-button').addEventListener('click', function() {
    fileInput.click();
});

fileInput.addEventListener('change', updateFileNameDisplay);

function updateFileNameDisplay() {
    var file = fileInput.files[0];
    if (file) {
        dragText.textContent = "Selected: " + file.name;
    } else {
        dragText.textContent = "Drag & Drop files here or";
    }
}
document.addEventListener("DOMContentLoaded", function () {
    
    function setupDragAndDrop(dragArea, fileInput) {
        const browseButton = dragArea.querySelector('.file1-browse-button');

        dragArea.addEventListener('dragover', function (e) {
            e.preventDefault();
            this.classList.add('file1-drag-over');
        });

        dragArea.addEventListener('dragleave', function () {
            this.classList.remove('file1-drag-over');
        });

        dragArea.addEventListener('drop', function (e) {
            e.preventDefault();
            fileInput.files = e.dataTransfer.files;
            const fileName = fileInput.files[0].name;
            const fileNameDisplay = this.querySelector('.file1-drag-text');
            fileNameDisplay.textContent = "Selected: " + fileName;
            this.classList.add('file1-drag-success');
            setTimeout(() => this.classList.remove('file1-drag-success'), 1000);
        });

        browseButton.addEventListener('click', function () {
            fileInput.click();
        });

        fileInput.addEventListener('change', function () {
            // Code to handle file input change
        });
    }
    
    const contentAreas = document.querySelectorAll('.content');  // Assuming the class 'content' uniquely identifies these two divs

    contentAreas.forEach((contentArea) => {
        const dragArea = contentArea.querySelector('.file1-drag-area');
        const fileInput = contentArea.querySelector('input[type="file"]');
        
        if (dragArea && fileInput) {
            setupDragAndDrop(dragArea, fileInput);
        }
    });
});

