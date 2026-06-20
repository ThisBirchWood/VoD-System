import React from "react";
import clsx from "clsx";

type Props = React.ButtonHTMLAttributes<HTMLButtonElement>;

const MenuButton: React.FC<Props> = ({ className = "", ...props }) => {
    return (
        <button
            className={clsx("p-2 rounded-md text-gray-600 hover:bg-gray-100 hover:text-gray-900 transition-colors duration-150", className)}
            {...props}
        />
    );
};

export default MenuButton;