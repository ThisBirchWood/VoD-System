// layout/MainLayout.jsx
import Sidebar from 'Frontend/components/Sidebar';
import { Outlet } from 'react-router-dom';

const MainLayout = () => (
    <div className="flex">
        <Sidebar />
        <div className="flex-1 p-4">
            <Outlet /> {/* This renders the nested route content */}
        </div>
    </div>
);

export default MainLayout;