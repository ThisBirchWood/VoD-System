import { Link } from "react-router-dom";
import clsx from "clsx";
import MenuButton from "./buttons/MenuButton.tsx";

type props = {
    className?: string
}

const Sidebar = ({className}: props) => {
    return (
        <div className={clsx("w-64 h-screen bg-white shadow-sm border-r px-4 py-6 flex flex-col gap-2", className)}>
            <Link
                to="/create">
                <MenuButton>
                    ➕ Create Clip
                </MenuButton>
            </Link>
        </div>
    );
};

export default Sidebar;