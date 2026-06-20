import React, {useEffect, useRef} from "react";
import clsx from "clsx";

type DropdownItemProps = {
    item: string;
    onClick: (item: string) => void;
    className?: string;
}

const DropdownItem = ({ item, onClick, className }: DropdownItemProps) => {
    return (
        <li className={clsx("cursor-pointer hover:bg-gray-50 px-3 py-2 text-sm text-gray-700", className)}
            onClick={() => onClick(item)}
        >
            {item}
        </li>
    );
}

type DropdownProps = {
    label: React.ReactNode;
    children: React.ReactNode;
    className?: string;
}

const Dropdown = ({ label, children, className }: DropdownProps) => {
    const [isOpen, setIsOpen] = React.useState(false);
    const ref = useRef<HTMLDivElement>(null);

    const toggleDropdown = () => {
        setIsOpen(!isOpen);
    };

    useEffect(() => {
        function handleClickOutside(event: { target: any }) {
            if (ref.current && !ref.current.contains(event.target)) {
                setIsOpen(false);
            }
        }

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    return (
        <div className={clsx(className, "relative inline-block text-left")}
            ref={ref}>
            <button
                onClick={toggleDropdown}
                className="inline-flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors duration-150"
            >
                {label}
            </button>
            {isOpen && (
                <ul className="absolute right-0 mt-1 w-36 origin-top-right rounded-lg bg-white border border-gray-200 shadow-md z-50 py-1 overflow-hidden">
                    {children}
                </ul>
            )}
        </div>
    );
};



export { Dropdown, DropdownItem };
