import React from 'react';

const AdminExportButton = () => {
    const handleExport = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('/api/vessels/static-info', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            // Add this line to see what you're actually getting
            const responseText = await response.text();
            console.log('Raw response:', responseText);

            if (!response.ok) {
                throw new Error('Failed to export data');
            }

            const data = JSON.parse(responseText);

            // Create and download JSON file
            const blob = new Blob([JSON.stringify(data, null, 2)], {
                type: 'application/json'
            });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'ship_static_data.json';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error exporting ships:', error);
            alert('Failed to export ship data');
        }
    };

    return (
        <button
            onClick={handleExport}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm"
        >
            Export Ships
        </button>
    );
};

export default AdminExportButton;