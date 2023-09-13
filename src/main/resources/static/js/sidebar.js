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
        "pdf-check-content",
        "signature-status-content1" // Add the ID of the new content div here
    ];
    
    idsToHide.forEach(function(id) {
        if (id !== elementId) {
            document.getElementById(id).style.display = 'none';
        }
    });

    toggleDisplay(elementId);
}
document.getElementById("show-button").addEventListener("click", function() {
    hideAllExcept("signature-status-content1");
});
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




function setupDragAndDrop(dragAreaId, fileInputId, textElementSelector) {
    const dragArea = document.getElementById(dragAreaId);
    const fileInput = document.getElementById(fileInputId);
    const dragText = document.querySelector(textElementSelector);

    if (dragArea && fileInput && dragText) {
        dragArea.addEventListener('dragover', function (e) {
            e.preventDefault();
            e.stopPropagation();
            this.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
        });

        dragArea.addEventListener('dragleave', function () {
            e.preventDefault();
            e.stopPropagation();
            this.style.backgroundColor = 'transparent';
        });

        dragArea.addEventListener('drop', function (e) {
            e.preventDefault();
            e.stopPropagation();
            this.style.backgroundColor = 'transparent';
            fileInput.files = e.dataTransfer.files;
            updateFileNameDisplay(fileInput, dragText);
        });

        dragText.addEventListener('click', function () {
            fileInput.click();
        });

        fileInput.addEventListener('change', function () {
            updateFileNameDisplay(fileInput, dragText);
        });
    } else {
        console.error("One or more elements not found: ", dragAreaId, fileInputId, textElementSelector);
    }
}


function updateFileNameDisplay(fileInput, dragText) {
    const file = fileInput.files[0];
    if (file) {
        dragText.textContent = "Selected: " + file.name;
    } else {
        dragText.textContent = "Drag & Drop files here or";
    }
}

document.addEventListener("DOMContentLoaded", function () {
    setupDragAndDrop('file1-upload-area', 'file1-input', '.file1-drag-text');
    setupDragAndDrop('file2-upload-area', 'file2-input', '.file2-drag-text');
    setupDragAndDrop('upload-area','file-input','.drag-text')
});
