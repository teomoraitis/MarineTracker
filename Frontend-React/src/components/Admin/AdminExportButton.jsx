import React from 'react';
import { getVesselsStaticData } from '../../api/vesselsApi';

const AdminExportButton = () => {
  const handleExport = async () => {
    try {
      console.log("export");
      const staticData = await getVesselsStaticData();
      console.log(staticData)

      // Create and download JSON file
      const blob = new Blob([JSON.stringify(staticData, null, 2)], {
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
      className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 text-xl"
    >
      Export Ships
    </button>
  );
};

export default AdminExportButton;