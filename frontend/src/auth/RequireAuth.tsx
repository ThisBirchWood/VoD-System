import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";

const RequireAuth = () => {
    const { user, loading } = useAuth();
    const location = useLocation();

    if (loading) return null;
    if (!user) return <Navigate to="/" replace state={{ from: location }} />;

    return <Outlet />;
};

export default RequireAuth;